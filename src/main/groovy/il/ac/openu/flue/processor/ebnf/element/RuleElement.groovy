package il.ac.openu.flue.processor.ebnf.element

import il.ac.openu.flue.processor.ebnf.element.AndList
import il.ac.openu.flue.processor.ebnf.element.OrList

/**
 * @author Noam Rotem
 */
trait RuleElement {
    AndList and(RuleElement e) {
        new AndList(this, e)
    }

    AndList and(Closure<?> c) {
        Object o = c()

        if (o instanceof RuleElement) {
            new AndList(this, new ZeroOrMore(o))
        } else if (o instanceof AndList) {
            new AndList(this, new ZeroOrMore(o.getElements()))
        } else throw new RuntimeException("Illegal closure type")
    }

    AndList and(List<RuleElement> l) {
        new AndList(this, new ZeroOrOne(l[0]))
    }

    AndList and(String s) {
        new AndList(this, new Token(s))
    }

    OrList or(RuleElement e) {
        new OrList(this, e)
    }

    OrList or(String s) {
        new OrList(this, new Token(s))
    }
}
