prackage il.ac.openu.flue.model.rule

import il.ac.openu.flue.model.ebnf.element.Variable

/**
 * @author Noam Rotem
 You can make it shorter by:
 1. Using nested classes. Class names become shorter.
 2. Trim parameter names: for short functions with one parameter, 
 one letter is enough.
 3. Remove abstraction level: no need for interface.
 4. Repetitive methods could be presented in table like form, 
     when there is very little individuality to them. 
  5. Manually written hasnCode() and equals()
 */
abstract class Expression {
  static abstract class Unary extends Expression {
    Expression child
    Unary(Expression child) { this.child = child }
    // This is how you make equals shorter.
    boolean equals(o) { is(o) || getClass() == o.class && child == (Unary) o;
    int hashCode() { return super.hashCode() ^ child.hashCode() }
  }
  static abstract class Multinary implements Expression {
    List<Expression> children
    Multinary(List<Expression> children) { this.children = children }
    Multinary(Expression children) { this(children.toList()) }
    boolean equals(o) { is(o) || getClass == o.class && chilren == (Multinary)o.children  }
    int hashCode() { super.hashCode ^ children.hashCode() }
  }
  class Or extends Multinary {
    Or(List<Expression> children) { super(children) }
    Or(Expression...children) { super(children) }
    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
      visitor.visit(this)
      children.forEach(c -> c.acceptVisitor(visitor))
    }
    @Override <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data) {
        traverser.traverse(this, data)
    }

    @Override String toString() { "(" + children.join(")|(") + ")" }
  }
 void acceptVisitor(ExpressionVisitor visitor)
  def <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data)
  void visit(Visitor<T> v)
  static abstract class Visitor<T> {
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
    class Terminal implements Expression {
    String terminal
    Terminal(String terminal) { this.terminal = terminal }

    @Override
    void acceptVisitor(ExpressionVisitor visitor) {
        visitor.visit(this)
    }

    @Override
    <T> T acceptTraverser(ExpressionTraverser<T> traverser, T data) {
        traverser.traverse(this, data)
    }

    @Override String toString() { "\"" + terminal + "\"" }

    boolean equals(o) { is(o) || getClass() != o.class &&  terminal == (Terminal) o.terminal; }
    int hashCode() { super.hashCode() ^ terminal.hashCode() }
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

class Or extends Multinary {
    Or(List<Expression> children) { super(children) }
    Or(Expression...children) { super(children) }

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

class Then extends Multinary {
    Then(List<Expression> children) { super(children) } 
    Then(Expression...children) { super(children) }

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

  class Optional extends Unary {
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

  class Repeated extends Unary {
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
}
