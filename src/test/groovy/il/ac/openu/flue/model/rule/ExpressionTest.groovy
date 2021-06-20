package il.ac.openu.flue.model.rule

import il.ac.openu.flue.model.ebnf.element.Variable
import spock.lang.Specification
import static il.ac.openu.flue.model.rule.ExpressionTest.V.*
import static il.ac.openu.flue.model.rule.Expression.Visitor

/**
 * @author Noam Rotem
 */
class ExpressionTest extends Specification {
    enum V implements Variable {A, B, C}

    static def nt(Variable v) {
        new NonTerminal(v)
    }

    Visitor<String> visitor = new Visitor<String>() {
        @Override
        String visit(Then then) {
            StringBuilder builder = new StringBuilder("then;")
            then.children.forEach(c -> builder.append(c.accept(this)))
            builder.toString()
        }

        @Override
        String visit(Or or) {
            StringBuilder builder = new StringBuilder("or;")
            or.children.forEach(c -> builder.append(c.accept(this)))
            builder.toString()
        }

        @Override
        String visit(Optional optional) {
            StringBuilder builder = new StringBuilder("optional;")
            builder.append(optional.child.accept(this))
            builder.toString()
        }

        @Override
        String visit(Repeated repeated) {
            StringBuilder builder = new StringBuilder("repeated;")
            builder.append(repeated.child.accept(this))
            builder.toString()
        }

        @Override
        String visit(NonTerminal nonTerminal) {
            nonTerminal.toString() + ";"
        }

        @Override
        String visit(Terminal terminal) {
            terminal.toString() + ";"
        }
    }

    def "testVisit"(Expression expression, String path) {
        expect:
             expression.accept(visitor) == path

        where:
            expression                                          | path
            nt(A)                                               | "A;"
            new Terminal("A")                                   | "\"A\";"
            new Or(nt(A), nt(B))                                | "or;A;B;"
            new Then(nt(A), nt(B))                              | "then;A;B;"
            new Then(new Optional(nt(A)), new Repeated(nt(B)))  | "then;optional;A;repeated;B;"
            new Optional(new Or(nt(A), new Repeated(nt(B))))    | "optional;or;A;repeated;B;"
    }

    def "testEquals"(Expression e1, Expression e2, boolean result) {
        expect:
            (e1 == e2) == result

        where:
            e1                          | e2                     | result
            nt(A)                       | nt(A)                  | true
            nt(A)                       | nt(B)                  | false
            new Terminal("A")           | new Terminal("A")      | true
            new Terminal("A")           | new Terminal("B")      | false
            new Or(nt(A), nt(B))        | new Or(nt(A), nt(B))   | true
            new Or(nt(A), nt(B))        | new Or(nt(B), nt(A))   | false
            new Or(nt(A), nt(B), nt(C)) | new Or(nt(A), nt(B))   | false
            new Then(nt(A), nt(B))      | new Then(nt(A), nt(B)) | true
            new Then(nt(A), nt(B))      | new Then(nt(B), nt(A)) | false
            new Then(nt(A), nt(B))      | new Or(nt(A), nt(B))   | false
            new Optional(nt(A))         | new Optional(nt(A))    | true
            new Optional(nt(A))         | new Optional(nt(B))    | false
            new Optional(nt(A))         | nt(A)                  | false
    }
}
