package il.ac.openu.flue.model.ebnf.extension

import il.ac.openu.flue.model.ebnf.element.*

/**
 * @author Noam Rotem
 */
class EBNFExtension {
    static AndList and(Closure<RuleElement> self, RuleElement e) {
        new AndList(new OneOrMore(self()), e)
    }

    static AndList and(Closure<RuleElement> self, Closure<RuleElement> c) {
        new AndList(new OneOrMore(self()), new OneOrMore(c()))
    }

    static AndList and(Closure<RuleElement> self, List<RuleElement> l) {
        new AndList(new OneOrMore(self()), new ZeroOrOne(l[0]))
    }

    static AndList and(Closure<RuleElement> self, String s) {
        new AndList(new OneOrMore(self()), new Token(s))
    }

    static AndList and(String self, RuleElement r) {
        new AndList(new Token(self), r)
    }

    static AndList and(String self, List<RuleElement> l) {
        new AndList(new Token(self), new ZeroOrOne(l[0]))
    }

    static AndList and(String self, Closure<?> c) {
        Object o = c()

        if (o instanceof RuleElement) {
            new AndList(new Token(self), o)
        } else if (o instanceof AndList) {
            List<RuleElement> lst = (o as AndList).getElements()
            lst.add(0, new Token(self))
            new AndList(lst)
        } else throw new RuntimeException("Illegal closure type")
    }

    static AndList and(String self, String s) {
        new AndList(new Token(self), new Token(s))
    }

    static AndList and(List<RuleElement> self, RuleElement e) {
        new AndList(new ZeroOrOne(self[0]), e)
    }

    static AndList and(List<RuleElement> self, Closure<RuleElement> c) {
        new AndList(new ZeroOrOne(self[0]), new OneOrMore(c()))
    }

    static AndList and(List<RuleElement> self, List<RuleElement> l) {
        new AndList(new ZeroOrOne(self[0]), new ZeroOrOne(l[0]))
    }

    static AndList and(List<RuleElement> self, String s) {
        new AndList(new ZeroOrOne(self[0]), new Token(s))
    }
}
