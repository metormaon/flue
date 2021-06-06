package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.rule.Expression

/**
 * @author Noam Rotem
 */
trait RuleElement {
    AndList and(RuleElement e) {
        if (e instanceof AndList) {
            AndList andList = new AndList(this)
            andList.elements.addAll(e.elements)
            andList
        } else {
            new AndList(this, e)
        }
    }

    AndList and(Closure<RuleElement> c) {
        new AndList(this, new OneOrMore(c))
    }

    AndList and(List<RuleElement> l) {
        new AndList(this, new ZeroOrOne(l))
    }

    AndList and(String s) {
        new AndList(this, new Token(s))
    }

    OrList or(RuleElement e) {
        if (e instanceof OrList) {
            OrList orList = new OrList(this)
            orList.elements.addAll(e.elements)
            orList
        } else {
            new OrList(this, e)
        }
    }

    OrList or(Closure<RuleElement> c) {
        new OrList(this, new OneOrMore(c))
    }

    OrList or(List<RuleElement> l) {
        new OrList(this, new ZeroOrOne(l))
    }

    OrList or(String s) {
        new OrList(this, new Token(s))
    }

    abstract Expression expression()
}
