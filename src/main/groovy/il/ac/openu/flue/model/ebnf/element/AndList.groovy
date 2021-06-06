package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.rule.Expression
import il.ac.openu.flue.model.rule.Then

/**
 * @author Noam Rotem
 */
class AndList implements RuleElement {
    List<RuleElement> elements = []

    AndList(RuleElement...e) {
        elements.addAll(e)
    }

    @Override
    AndList and(RuleElement e) {
        elements.add(e)
        this
    }

    @Override
    AndList and(Closure<RuleElement> c) {
        elements.add(new OneOrMore(c))
        this
    }

    @Override
    AndList and(List<RuleElement> l) {
        elements.add(new ZeroOrOne(l))
        this
    }

    @Override
    AndList and(String s) {
        elements.add(new Token(s))
        this
    }

    @Override
    Expression expression() {
        new Then(elements.collect{it.expression()})
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        AndList andList = (AndList) o

        if (elements != andList.elements) return false

        return true
    }

    int hashCode() {
        return elements.hashCode()
    }
}
