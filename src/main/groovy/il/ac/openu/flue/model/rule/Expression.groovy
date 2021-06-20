package il.ac.openu.flue.model.rule

import groovy.transform.EqualsAndHashCode
import il.ac.openu.flue.model.ebnf.element.Variable

/**
 * @author Noam Rotem
 */
interface Expression {
    def <T> T accept(Visitor<T> v)

    @SuppressWarnings('GrMethodMayBeStatic')
    abstract class Visitor<T> {
        T visit(Then then) { null }
        T visit(Or or) { null }
        T visit(Optional optional) { null }
        T visit(Repeated repeated) { null }
        T visit(NonTerminal nonTerminal) { null }
        T visit(Terminal terminal) { null }
    }
}

@EqualsAndHashCode(includeFields=true)
abstract class Unary implements Expression {
    Expression child
    Unary(Expression child) { this.child = child }
}

@EqualsAndHashCode(includeFields=true)
abstract class Multinary implements Expression {
    List<Expression> children
    Multinary(List<Expression> children) { this.children = children }
    Multinary(Expression...children) { this(children.toList()) }
}

@EqualsAndHashCode(includeFields=true)
class Terminal implements Expression {
    String terminal
    Terminal(String terminal) { this.terminal = terminal }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override String toString() { "\"" + terminal + "\"" }
}

@EqualsAndHashCode(includeFields=true)
class NonTerminal implements Expression {
    Variable variable

    NonTerminal(Variable variable) { this.variable = variable }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override String toString() { variable }
}

@EqualsAndHashCode(callSuper=true)
class Or extends Multinary {
    Or(List<Expression> children) { super(children) }
    Or(Expression...children) { super(children) }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override String toString() { "(" + children.join(")|(") + ")" }
}

@EqualsAndHashCode(callSuper=true)
class Then extends Multinary {
    Then(List<Expression> children) { super(children) }
    Then(Expression...children) { super(children) }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override
    String toString() { "(" + children.join(")&(") + ")" }
}

@EqualsAndHashCode(callSuper=true)
class Optional extends Unary {
    Optional(Expression child) { super(child) }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override String toString() { "[" + child + "]" }
}

@EqualsAndHashCode(callSuper=true)
class Repeated extends Unary {
    Repeated(Expression child) { super(child) }
    @Override <T> T accept(Visitor<T> v) { v.visit(this) }
    @Override String toString() { "{" + child + "}" }
}
