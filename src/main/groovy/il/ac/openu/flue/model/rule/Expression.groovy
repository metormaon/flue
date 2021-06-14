prackage il.ac.openu.flue.model.rule

import il.ac.openu.flue.model.ebnf.element.Variable

/**
 * @author Noam Rotem
 */
interface Expression {
    void acceptVisitor(ExpressionVisitor visitor)
    def <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data)
    void visit(Visitor<T> v)
    abstract class Visitor<T> {
      T visit(Then then) 
      T visit(Or or)
      T visit(Optional optional)
      T visit(Repeated repeated)
      T visit(NonTerminal nonTerminal)
      T visit(Terminal terminal)
    }
    Map<Symbol, Set<Symbol>> badFirst(Expression e) {
      e.visit(new Visitor<Set<Terminal>>() {
        T visit(Then then)
        T visit(Or or)
        T visit(Optional optional)
        T visit(Repeated repeated)
        T visit(NonTerminal nonTerminal)
        T visit(Terminal terminal)
    }
    abstract class Unary implements Expression {
    Expression child
    UnaryExpression(Expression child) { this.child = child }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        UnaryExpression unaryExpression = (UnaryExpression) o

        if (child != unaryExpression.child) return false

        return true
    }

    int hashCode() { return super.hashCode() + child.hashCode() }
    abstract class MultinaryExpression implements Expression {
    List<Expression> children

    Multinary(List<Expression> children) { this.children = children }

    Multinary(Expression...children) {
        this(children.toList())
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Multinary multinaryExpression = (MultinaryExpression) o

        if (children != multinaryExpression.children) return false

        return true
    }

    int hashCode() {
        return children.hashCode()
    }
}

}

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
}
}

class Collector extends ExpressionVisitor {


class Terminal implements Expression {
    String terminal

    Terminal(String terminal) {
        this.terminal = terminal
    }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this)
    }

    @Override
    <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data) {
        traverser.traverse(this, data)
    }

    @Override String toString() { "\"" + terminal + "\"" }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Terminal terminal1 = (Terminal) o

        if (terminal != terminal1.terminal) return false

        return true
    }

    int hashCode() { super.hashCode() + terminal.hashCode() }
}

class NonTerminal implements Expression {
    Variable variable

    NonTerminal(Variable variable) {
        this.variable = variable
    }

    @Override
    void acceptVisitor(ExpressionVisitor v) { v.visit(this) }

    @Override
    <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data) {
        traverser.traverse(this, data)
    }

    @Override
    String toString() { variable }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        NonTerminal that = (NonTerminal) o

        if (variable != that.variable) return false

        return true
    }

    int hashCode() { super.hashCode() ^ variable.hashCode() }
}

class Or extends MultinaryExpression {
    Or(List<Expression> children) {
        super(children)
    }

    Or(Expression...children) {
        super(children)
    }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this)
        children.forEach(c -> c.acceptVisitor(visitor))
    }

    @Override
    <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data) {
        traverser.traverse(this, data)
    }

    @Override
    String toString() {
        "(" + children.join(")|(") + ")"
    }
}

class Then extends MultinaryExpression {
    Then(List<Expression> children) {
        super(children)
    }

    Then(Expression...children) {
        super(children)
    }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this)
        children.forEach(c -> c.acceptVisitor(visitor))
    }

    @Override
    <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data) {
        traverser.traverse(this, data)
    }

    @Override
    String toString() {
        "(" + children.join(")&(") + ")"
    }
}

class Optional extends UnaryExpression {
    Optional(Expression child) { super(child) }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this)
        child.acceptVisitor(visitor)
    }

    @Override
    <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data) {
        traverser.traverse(this, data)
    }

    @Override
    String toString() {
        "[" + child + "]"
    }
}

class Repeated extends UnaryExpression {
    Repeated(Expression child) { super(child) }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this)
        child.acceptVisitor(visitor)
    }

    @Override
    <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data) {
        traverser.traverse(this, data)
    }

    @Override String toString() { "{" + child + "}" }
}
