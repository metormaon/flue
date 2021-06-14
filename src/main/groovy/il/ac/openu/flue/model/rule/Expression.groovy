package il.ac.openu.flue.model.rule

import il.ac.openu.flue.model.ebnf.element.Variable

/**
 * @author Noam Rotem
 */
interface Expression {
    void acceptVisitor(ExpressionVisitor visitor)
    def <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data)
}

abstract class UnaryExpression implements Expression {
    Expression child

    UnaryExpression(Expression child) {
        this.child = child
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        UnaryExpression unaryExpression = (UnaryExpression) o

        if (child != unaryExpression.child) return false

        return true
    }

    int hashCode() {
        return child.hashCode()
    }
}

abstract class MultinaryExpression implements Expression {
    List<Expression> children

    MultinaryExpression(List<Expression> children) {
        this.children = children
    }

    MultinaryExpression(Expression...children) {
        this(children.toList())
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        MultinaryExpression multinaryExpression = (MultinaryExpression) o

        if (children != multinaryExpression.children) return false

        return true
    }

    int hashCode() {
        return children.hashCode()
    }
}

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

    @Override
    String toString() {
        "\"" + terminal + "\""
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Terminal terminal1 = (Terminal) o

        if (terminal != terminal1.terminal) return false

        return true
    }

    int hashCode() {
        return terminal.hashCode()
    }
}

class NonTerminal implements Expression {
    Variable variable

    NonTerminal(Variable variable) {
        this.variable = variable
    }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this)
    }

    @Override
    <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data) {
        traverser.traverse(this, data)
    }

    @Override
    String toString() {
        variable
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        NonTerminal that = (NonTerminal) o

        if (variable != that.variable) return false

        return true
    }

    int hashCode() {
        return variable.hashCode()
    }
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
    Optional(Expression child) {
        super(child)
    }

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
    Repeated(Expression child) {
        super(child)
    }

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
        "{" + child + "}"
    }
}
