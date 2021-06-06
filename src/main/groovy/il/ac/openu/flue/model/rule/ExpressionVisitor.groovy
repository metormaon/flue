package il.ac.openu.flue.model.rule

/**
 * @author Noam Rotem
 */
interface ExpressionVisitor{
    void visit(Then then)
    void visit(Or or)
    void visit(Optional optional)
    void visit(Repeated repeated)
    void visit(NonTerminal nonTerminal)
    void visit(Terminal terminal)
}

class ExpressionVisitorBase implements ExpressionVisitor {
    @Override
    void visit(Then then) {
    }

    @Override
    void visit(Or or) {
    }

    @Override
    void visit(Optional optional) {
    }

    @Override
    void visit(Repeated repeated) {
    }

    @Override
    void visit(NonTerminal nonTerminal) {
    }

    @Override
    void visit(Terminal terminal) {
    }
}