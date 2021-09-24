package il.ac.openu.flue.model.rule

import spock.lang.Specification
import static il.ac.openu.flue.model.rule.ExpressionTest.V.*
import static il.ac.openu.flue.model.rule.Expression.Visitor

/**
 * @author Noam Rotem
 */
class ExpressionTest extends Specification {
    enum V implements NonTerminal {A, B, C}

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
            expression                                  | path
            A                                           | "A;"
            new Terminal("A")                           | "\"A\";"
            new Or(A, B)                                | "or;A;B;"
            new Then(A, B)                              | "then;A;B;"
            new Then(new Optional(A), new Repeated(B))  | "then;optional;A;repeated;B;"
            new Optional(new Or(A, new Repeated(B)))    | "optional;or;A;repeated;B;"
    }

    def "testEquals"(Expression e1, Expression e2, boolean result) {
        expect:
            (e1 == e2) == result

        where:
            e1                  | e2                | result
            A                   | A                 | true
            A                   | B                 | false
            new Terminal("A")   | new Terminal("A") | true
            new Terminal("A")   | new Terminal("B") | false
            new Or(A, B)        | new Or(A, B)      | true
            new Or(A, B)        | new Or(B, A)      | false
            new Or(A, B, C)     | new Or(A, B)      | false
            new Then(A, B)      | new Then(A, B)    | true
            new Then(A, B)      | new Then(B, A)    | false
            new Then(A, B)      | new Or(A, B)      | false
            new Optional(A)     | new Optional(A)   | true
            new Optional(A)     | new Optional(B)   | false
            new Optional(A)     | A                 | false
    }

    def "testToString"() {
        expect:
            new Or(A, new Optional(new Then(new Terminal(","), A))).toString() == "(A)|([(\",\")&(A)])"
            new Or(A, new Repeated(new Then(new Terminal(","), A))).toString() == "(A)|({(\",\")&(A)})"
            new Or(A, new Repeated(new Then(new Terminal(","), A), null, true)).toString()
                    == "(A)|(+{(\",\")&(A)})"

            new Or(A, new Repeated(new Then(new Terminal(","), A), new Terminal(":"))).toString()
                    == "(A)|({(\",\")&(A)}/\":\")"

            new Or(A, new Repeated(new Then(new Terminal(","), A), new Terminal(":"),
                    true)).toString() == "(A)|(+{(\",\")&(A)}/\":\")"
    }
}
