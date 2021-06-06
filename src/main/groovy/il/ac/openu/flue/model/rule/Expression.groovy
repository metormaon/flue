package il.ac.openu.flue.model.rule

import il.ac.openu.flue.model.ebnf.element.Variable

/**
 * @author Noam Rotem
 */
interface Expression {
    void acceptVisitor(ExpressionVisitor visitor)
    def <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data)
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

class Or implements Expression {
    List<Expression> children

    Or(List<Expression> children) {
        this.children = children
    }

    Or(Expression...children) {
        this(children.toList())
    }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this);
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Or or = (Or) o

        if (children != or.children) return false

        return true
    }

    int hashCode() {
        return children.hashCode()
    }
}

class Then implements Expression {
    List<Expression> children

    Then(List<Expression> children) {
        this.children = children
    }

    Then(Expression ... children) {
        this(children.toList())
    }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this);
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Then then = (Then) o

        if (children != then.children) return false

        return true
    }

    int hashCode() {
        return children.hashCode()
    }
}

class Optional implements Expression {
    Expression child

    Optional(Expression child) {
        this.child = child
    }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this);
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Optional optional = (Optional) o

        if (child != optional.child) return false

        return true
    }

    int hashCode() {
        return child.hashCode()
    }
}

class Repeated implements Expression {
    Expression child

    Repeated(Expression child) {
        this.child = child
    }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this);
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Repeated repeated = (Repeated) o

        if (child != repeated.child) return false

        return true
    }

    int hashCode() {
        return child.hashCode()
    }
}
