package il.ac.openu.flue.model.ebnf

//import il.ac.openu.flue.model.ebnf.element.Variable
//import il.ac.openu.flue.model.rule.Expression
//import il.ac.openu.flue.model.rule.Variable
//import il.ac.openu.flue.model.rule.Optional
//import il.ac.openu.flue.model.rule.Or
//import il.ac.openu.flue.model.rule.Repeated
//import il.ac.openu.flue.model.rule.Rule
//import il.ac.openu.flue.model.rule.Terminal
//import il.ac.openu.flue.model.rule.Then
//import il.ac.openu.flue.model.ebnf.AST.ASTClass.ASTField
//import jdk.nashorn.api.tree.LiteralTree
//
//import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Noam Rotem
 */
class AST {
//    protected static class ASTClass {
//        protected static class ASTField {
//            String name
//            ASTClass astClass
//            boolean isOptional
//            boolean isList
//            boolean isString
//            static Map<String, AtomicInteger> sequences = [:].withDefault { key -> new AtomicInteger(1) }
//        }
//
//        String name
//        boolean isInterface
//        Set<ASTClass> parents = [] as Set<ASTClass>
//        boolean isOptional
//        boolean isRedundant
//        List<ASTField> fields = []
//
//        @Override
//        String toString() {
//            StringBuilder image = new StringBuilder()
//            image.with {
//                append(isInterface ? "interface " : "class ")
//                append(name)
//                if (!parents.isEmpty()) {
//                    append(" implements ")
//                    append(parents.collect{it.name}.join(", "))
//                }
//                append(" {")
//                append(fields.collect {
//                    assert !(it.isOptional && it.isList)
//                    String wrap
//                    if (it.isList) {
//                        wrap = "List"
//                    } else if (it.isOptional) {
//                        wrap = "Optional"
//                    }
//
//                    String type = it.isString? "String" : it.astClass.name
//
//                    if (wrap) {
//                        type = "$wrap<$type>"
//                    }
//
//                    "$type ${it.name}"
//                }.join("; "))
//                append("}")
//            }
//
//            image.toString()
//        }
//    }
//
//    public final Map<String, ASTClass> astClasses
//    protected final Map<Variable, Rule> ruleMap
//
//    AST(Map<Variable, List<Rule>> rules) {
//        ruleMap = [:]
//
//        //Merge A -> B, A -> C into A -> B | C
//        rules.each {v, r ->
//            if (r.size() == 1) {
//                ruleMap[v] = r[0]
//            } else {
//                ruleMap[v] = new Rule(v, new Or(r.collect {it.definition }))
//            }
//        }
////TODO: with default
//        astClasses = new HashMap<String, ASTClass>().withDefault { key ->
//            new ASTClass(name: key.capitalize())
//        }
//
//        processAST(ruleMap)
//    }
//
//    private void processAST(Map<Variable, Rule> ruleMap) {
//        ruleMap.each {variable, rule ->
//            List<Expression> orElements
//
//            if (rule.definition instanceof Or) {
//                orElements = ((Or)rule.definition).children
//            } else {
//                orElements = [rule.definition]
//            }
//
//            //TODO: address A -> "x" & B | "y" & B. IF we clean non informational, we get equality. So we need to
//            //TODO: create an enum for the options
//
//            orElements.removeAll {!isInformational(it)}
//
//            if (orElements.isEmpty()) {
//                //TODO TODO TODO TODO
//            }
//
//            orElements.each {
//                List<Expression> thenElements
//
//                if (it instanceof Then) {
//                    thenElements = ((Then) it).children
//                } else {
//                    thenElements = [it]
//                }
//
//                //Removing non informational
//                thenElements.removeAll { !isInformational(it) }
//
//                int orCount = orElements.size()
//                int thenCount = thenElements.size()
//
//                if (thenCount == 0) {
//                    if (orCount == 1) {
//                        //A -> <non_informational> - means that A is never important, and any class that has A as
//                        //a field, should remove it
//                        astClasses[variable.label].isRedundant = true
//                    } else {
//                        //A -> B | C | <non_informational> - means that A could be B or C or <none of them and
//                        // not interesting>. In other words - any class that has A as a field, should have the field
//                        // optional...
//                        astClasses[variable.label].isOptional = true
//                    }
//                } else if (thenCount == 1 && thenElements[0] instanceof Variable) {
//                    //A -> B - means that B implements A, which is an interface.
//                    ASTClass astClass = astClasses[variable.label]
//                    astClass.isInterface = true
//                    astClasses[(thenElements[0] as Variable).variable.label].parents += astClass
//                } else if (orCount == 1) {
//                    //A -> B & C, A -> [B], A -> {B} - mean that A is a class with the right side as fields
//                    ASTClass astClass = astClasses[variable.label]
//                    astClass.fields.addAll(thenElements.collect{createField(it) })
//                } else {
//                    //A -> B & C | D, A -> [B] | D, A -> {B} | D - mean that A is the parent interface of a generated
//                    //new class that implements A and has the fields for B, C or [B] or {B}
//                    String newTypeName = variable.label + ASTField.sequences[variable.label].getAndIncrement()
//                    ASTClass newClass = new ASTClass(name: newTypeName, parents: [astClasses[variable.label]])
//                    newClass.fields.addAll(thenElements.collect{createField(it) })
//                    astClasses[newTypeName] = newClass
//                }
//            }
//        }
//
//        Set<ASTClass> optionalClasses = astClasses
//                .findAll {_, c -> c.isOptional}
//                .collect {it.value}
//
//        astClasses.each {_, c -> {
//            c.fields.each {f -> if (f.astClass in optionalClasses && !f.isList) f.isOptional = true}
//        }}
//    }
//
//    ASTField createField(Expression e) {
//        switch (e) {
//            case Terminal:
//                return new ASTField(name: "input${ASTField.sequences.input.getAndIncrement()}", isString: true)
//            case Variable:
//                Variable variable = (e as Variable).variable
//                String type = variable.label.uncapitalize()
//                return new ASTField(name: "$type${ASTField.sequences[type].getAndIncrement()}",
//                        astClass: astClasses[variable.label])
//            case Repeated:
//                ASTField field = createField((e as Repeated).child)
//                field.isList = true
//                return field
//            case Optional:
//                ASTField field = createField((e as Optional).child)
//                field.isOptional = true
//                return field
//            default:
//                throw new IllegalArgumentException("Cannot create a field for ${e}")
//        }
//    }
//
//    private boolean isInformational(Expression e) {
//        Set<Variable> visited = [] as Set<Variable>
//
//        Expression.Visitor<Boolean> informationalVisitor = new Expression.Visitor<Boolean>() {
//            @Override Boolean visit(Then then) { then.children.any {it.accept(this)} }
//            @Override Boolean visit(Or or) { true }
//            @Override Boolean visit(Optional optional) { true }
//            @Override Boolean visit(Repeated repeated) { true }
//            @Override Boolean visit(Variable nonTerminal) {
//                if (nonTerminal.variable in visited) {
//                    true
//                } else {
//                    visited += nonTerminal.variable
//                    ruleMap[nonTerminal.variable].any {it.definition.accept(this)}
//                }
//            }
//            @Override Boolean visit(Terminal terminal) { !(terminal.terminal ==~ /([a-zA-Z]+)|([^a-zA-Z0-9]+)/) }
//        }
//
//        e.accept(informationalVisitor)
//    }
//
//    @Override
//    String toString() {
//
//        astClasses.values() findAll {!it.isRedundant} join("\n")
//    }
//
//    @Override
//    boolean equals(o) {
//        if (this.is(o)) return true
//        if (getClass() != o.class) return false
//
//        AST ast = (AST) o
//
//        if (astClasses != ast.astClasses) return false
//
//        return true
//    }
//
//    @Override
//    int hashCode() {
//        return astClasses.hashCode()
//    }
}
