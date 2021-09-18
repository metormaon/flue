//file:noinspection UnnecessaryQualifiedReference
package il.ac.openu.flue.model.util

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import il.ac.openu.flue.model.rule.*

/**
 * ExPath holds a followable path to a sub-expression within an expression. It consists of PathNodes, which represent
 * the nodes of a path in the expression composite tree. Each node points to the sub-expression it represents. If
 * the sub-expression is a Multinary expression, and it is not the last node in the path, it also remembers the serial
 * of the child via which the path continues.
 * <p/>
 * For example, the path to D in the expression B | (C & [D]), is made of the following nodes:
 * <p/>
 * 1. Multinary node that points to an Or expression, and keeps the position of the next node: 1
 * (0 is B; 1 is C & [D]).
 * <p/>
 * 2. Multinary node that points to a Then expression, and keeps the position of the next node: 1
 * (0 is C; 1 is [D]).
 * <p/>
 * 3. A PathNode that points to an Optional (i.e. - to [D]).
 * <p/>
 * 4. A PathNode that points to D.
 *
 * The method match() (which might as well be moved to the EBNF class) uses a closure parameter to find all the
 * sub-expressions in a provided expression, that match some specified pattern or behavior. The function will
 * traverse the expression composite tree, apply the closure on each node, and build an ExPath to each node that
 * matched the criterion (i.e. - for which the closure has evaluated to True).
 *
 * @author Noam Rotem
 */
class ExPath {
    static class PathNode {
        Expression expression

        @Override
        String toString() {
            switch (expression) {
                case Repeated:
                    "{}"
                    break
                case Optional:
                    "[]"
                    break
                case NonTerminal:
                    expression as NonTerminal
                    break
                case Terminal:
                    "'" + (expression as Terminal).terminal + "'"
                    break
                case Then:
                case Or:
                    "`" + expression.toString() + "`"
                    break
                default:
                    throw new IllegalStateException("Bad path node")
            }
        }

        /**
         * Builds a node for an expression
         */
        static PathNode of(Expression e) {
            new PathNode(expression: e)
        }

        /**
         * Builds a multinary node for an expression
         */
        static PathNode of(Expression e, int position) {
            new PathMultinaryNode(expression: e, positionOfNext: position)
        }
}

    //Represents parent of next. If there's no next, it's the parent of the expression to which the entire path leads
    static class PathMultinaryNode extends PathNode {
        int positionOfNext

        @Override
        String toString() {
            switch (expression) {
                case Then:
                    "&" + positionOfNext
                    break
                case Or:
                    "|" + positionOfNext
                    break
                default:
                    throw new IllegalStateException("Bad path node")
            }
        }
    }

    List<PathNode> path

    @Override
    String toString() {
        "*/" + path.join("/")
    }

    static ExPath of(PathNode ... nodes) {
        new ExPath(path: nodes)
    }

    static ExPath of(List<PathNode> nodes) {
        new ExPath(path: nodes)
    }

    static List<ExPath> match(Expression e, @ClosureParams(value = SimpleType,
            options = "il.ac.openu.flue.model.rule.Expression") Closure<Boolean> criterion) {
        e.accept(new PathVisitor(criterion))
    }

    static class PathVisitor extends Expression.Visitor<List<ExPath>> {
        Closure<Boolean> criterion

        PathVisitor(@ClosureParams(value = SimpleType,
                options = "il.ac.openu.flue.model.rule.Expression") Closure<Boolean> expressionTest) {
            this.criterion = expressionTest
        }

        @Override
        List<ExPath> visit(Then then) {
            List<ExPath> exPaths = []

            //Wrap the current expression element as a PathNode to shortcut the next actions
            PathNode self = PathNode.of(then)

            //If it's this Then that we are looking for - put it in the result!
            if (criterion.call(then)) {
                exPaths += [ExPath.of(self)]
            }

            //But instead or in addition, what we search for might be inside the Then. So recurse into it.
            //Prepend any result found within children with a node representing the Then and the position of the child
            then.children.eachWithIndex{it, i -> {
                it.accept(this).each {ex ->
                    exPaths += ExPath.of([PathNode.of(then, i), *ex.path])
                }
            }}

            exPaths
        }

        @Override
        List<ExPath> visit(Or or) {
            List<ExPath> exPaths = []

            //Wrap the current expression element as a PathNode to shortcut the next actions
            PathNode self = PathNode.of(or)

            //If it's this Or that we are looking for - put it in the result!
            if (criterion.call(or)) {
                exPaths += [ExPath.of(self)]
            }

            //But instead or in addition, what we search for might be inside the Or. So recurse into it.
            //Prepend any result found within children with a node representing the Or and the position of the child
            or.children.eachWithIndex{it, i -> {
                it.accept(this).each {ex ->
                    exPaths += ExPath.of([PathNode.of(or, i), *ex.path])
                }
            }}

            exPaths
        }

        @Override
        List<ExPath> visit(Optional optional) {
            List<ExPath> exPaths = []

            //Wrap the current expression element as a PathNode to shortcut the next actions
            PathNode self = PathNode.of(optional)

            //If it's this Optional that we are looking for - put it in the result!
            if (criterion.call(optional)) {
                exPaths += [ExPath.of(self)]
            }

            //But instead or in addition, what we search for might be inside the optional. So recurse into it.
            List<ExPath> childExPaths = optional.child.accept(this)

            //To each result found inside, add the optional as a parent (the * breaks a list into its members)
            exPaths += childExPaths.collect {ExPath e -> ExPath.of([self, *e.path] as List<PathNode>)}

            exPaths
        }

        @Override
        List<ExPath> visit(Repeated repeated) {
            List<ExPath> exPaths = []

            //Wrap the current expression element as a PathNode to shortcut the next actions
            PathNode self = PathNode.of(repeated)

            //If it's this Repeated that we are looking for - put it in the result!
            if (criterion.call(repeated)) {
                exPaths += [ExPath.of(self)]
            }

            //But instead or in addition, what we search for might be inside the repeated. So recurse into it.
            List<ExPath> childExPaths = repeated.child.accept(this)

            //To each result found inside, add the repeated as a parent (the * breaks a list into its members)
            exPaths += childExPaths.collect {ExPath e -> ExPath.of([self, *e.path] as List<PathNode>)}

            exPaths
        }

        @Override
        List<ExPath> visit(NonTerminal nonTerminal) {
            //If it's this non terminal that we are looking for - put it in the result!
            if (criterion.call(nonTerminal)) {
                [ExPath.of(PathNode.of(nonTerminal))]
            } else {
                []
            }
        }

        @Override
        List<ExPath> visit(Terminal terminal) {
            //If it's this terminal that we are looking for - put it in the result!
            if (criterion.call(terminal)) {
                [ExPath.of(PathNode.of(terminal))]
            } else {
                []
            }
        }
    }
}
