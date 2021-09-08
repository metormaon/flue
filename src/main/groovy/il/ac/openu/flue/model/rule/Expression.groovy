package il.ac.openu.flue.model.rule

import groovy.transform.EqualsAndHashCode
import groovy.transform.SelfType
import il.ac.openu.flue.model.ebnf.EBNF

/**
 * @author Noam Rotem
 */
trait Expression {
    Or or(Expression e) {
        List<Expression> children = []

        if (this instanceof Or) children += (this as Or).children
        else children += this

        if (e instanceof Or) children += (e as Or).children
        else children += e

        new Or(children)
    }
    Or or(Closure<?> c) { new Or(this, new Repeated(c)) }
    Or or(List<?> l) { new Or(this, new Optional(l)) }
    Or or(String s) { new Or(this, new Terminal(s)) }

    Then and(Expression e) {
        List<Expression> children = []

        if (this instanceof Then) children += (this as Then).children
        else children += this

        if (e instanceof Then) children += (e as Then).children
        else children += e

        new Then(children)
    }
    Then and(Closure<?> c) { new Then(this, new Repeated(c)) }
    Then and(List<?> l) { new Then(this, new Optional(l)) }
    Then and(String s) { new Then(this, new Terminal(s)) }

    abstract <T> T accept(Visitor<T> v)

    @SuppressWarnings('GrMethodMayBeStatic')
    static abstract class Visitor<T> {
        T visit(Then then) { null }
        T visit(Or or) { null }
        T visit(Optional optional) { null }
        T visit(Repeated repeated) { null }
        T visit(NonTerminal nonTerminal) { null }
        T visit(Terminal terminal) { null }
    }
}

@SelfType(Enum)
trait Labeled {
    String label = name()

    @Override
    String toString() {
        label
    }
}

@EqualsAndHashCode
abstract class Unary implements Expression {
    Expression child
    Unary(Expression child) { this.child = child }
}

@EqualsAndHashCode
abstract class Multinary implements Expression {
    List<Expression> children
    Multinary(List<Expression> children) { this.children = children }
    Multinary(Expression...children) { this(children.toList()) }
}

@EqualsAndHashCode
class Terminal implements Expression {
    @SuppressWarnings('NonAsciiCharacters')
    public static final Terminal ε = new Terminal("ε")
    String terminal
    Terminal(String terminal) { this.terminal = terminal }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override String toString() { "\"" + terminal + "\"" }
}

@EqualsAndHashCode
trait NonTerminal implements Labeled, Expression {
    Rule rightShift(Expression e) { EBNF.add(new Rule(this, e)) }
    Rule rightShift(List<?> l) { EBNF.add(new Rule(this, new Optional(l))) }
    Rule rightShift(Closure<?> c) { EBNF.add(new Rule(this, new Repeated(c))) }
    Rule rightShift(String s) { EBNF.add(new Rule(this, new Terminal(s))) }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
}

@EqualsAndHashCode(callSuper=true)
class Or extends Multinary {
    Or(List<Expression> children) { super(children) }
    Or(Expression...children) { super(children) }
    @Override Or or(Expression e) { children.add(e); this }
    @Override Or or(Closure<?> c) { children.add(new Repeated(c)); this }
    @Override Or or(List<?> l) { children.add(new Optional(l)); this }
    @Override Or or(String s) { children.add(new Terminal(s)); this }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override String toString() { "(" + children.join(")|(") + ")" }
}

@EqualsAndHashCode(callSuper=true)
class Then extends Multinary {
    Then(List<Expression> children) { super(children) }
    Then(Expression...children) { super(children) }
    @Override Then and(Expression e) { children.add(e); this }
    @Override Then and(Closure<?> c) { children.add(new Repeated(c)); this }
    @Override Then and(List<?> l) { children.add(new Optional(l)); this }
    @Override Then and(String s) { children.add(new Terminal(s)); this }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override
    String toString() { "(" + children.join(")&(") + ")" }
}

@EqualsAndHashCode(callSuper=true)
class Optional extends Unary {
    Optional(Expression child) { super(child) }
    Optional(List<?> l) { this(l[0] as Expression) }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override String toString() { "[" + child + "]" }
}

@EqualsAndHashCode(callSuper=true)
class Repeated extends Unary {
    Repeated(Expression child) { super(child) }
    Repeated(Closure<?> child) { this(child() as Expression) }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override String toString() { "{" + child + "}" }
}
