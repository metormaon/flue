package il.ac.openu.flue.model.rule

/**
 * @author Noam Rotem
 */
interface ExpressionTraverser<T> {
    T traverse(NonTerminal nonTerminal, T state)
    T traverse(Terminal terminal, T state)
    T traverse(Then then, T state)
    T traverse(Or or, T state)
    T traverse(Optional optional, T state)
    T traverse(Repeated repeated, T state)
}

abstract class ExpressionTraverserBase<T> implements ExpressionTraverser<T> {
    @Override
    T traverse(NonTerminal nonTerminal, T state) {
        state
    }

    @Override
    T traverse(Terminal terminal, T state) {
        state
    }

    @Override
    T traverse(Then then, T state) {
        state
    }

    @Override
    T traverse(Or or, T state) {
        state
    }

    @Override
    T traverse(Optional optional, T state) {
        state
    }

    @Override
    T traverse(Repeated repeated, T state) {
        state
    }
}