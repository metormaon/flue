package il.ac.openu.flue.model.rule

import il.ac.openu.flue.model.ebnf.element.Variable
import spock.lang.Specification
import static il.ac.openu.flue.model.rule.ExpressionTest.V.*

/**
 * @author Noam Rotem
 */
class ExpressionTest extends Specification {
    enum V implements Variable {A, B, C}

    class TestVisitor implements ExpressionVisitor {
        StringBuilder path = new StringBuilder()

        @Override
        void visit(Then then) {
            path.append(then).append(";")
        }

        @Override
        void visit(Or or) {
            path.append(or).append(";")
        }

        @Override
        void visit(Optional optional) {
            path.append(optional).append(";")
        }

        @Override
        void visit(Repeated repeated) {
            path.append(repeated).append(";")
        }

        @Override
        void visit(NonTerminal nonTerminal) {
            path.append(nonTerminal).append(";")
        }

        @Override
        void visit(Terminal terminal) {
            path.append(terminal).append(";")
        }
    }

    static def nt(Variable v) {
        new NonTerminal(v)
    }

    TestVisitor visitor

    void setup() {
        visitor = new TestVisitor()
    }

    def "testVisit"(Expression expression, String path) {
        expect:
            expression.acceptVisitor(visitor)
            visitor.path.toString() == path

        where:
            expression                                          | path
            nt(A)                                               | "A;"
            new Terminal("A")                                   | "\"A\";"
            new Or(nt(A), nt(B))                                | "(A)|(B);A;B;"
            new Then(nt(A), nt(B))                              | "(A)&(B);A;B;"
            new Then(new Optional(nt(A)), new Repeated(nt(B)))  | "([A])&({B});[A];A;{B};B;"
            new Optional(new Or(nt(A), new Repeated(nt(B))))    | "[(A)|({B})];(A)|({B});A;{B};B;"
    }
}
