package il.ac.openu.flue.model.ebnf.extension

import il.ac.openu.flue.model.ebnf.element.*

/**
 * @author Noam Rotem
 */
class EBNFExtension {
    static AndList and(Closure<RuleElement> self, RuleElement e) {
        new AndList(new OneOrMore(self), e)
    }

    static AndList and(Closure<RuleElement> self, Closure<RuleElement> c) {
        new AndList(new OneOrMore(self), new OneOrMore(c))
    }

    static AndList and(Closure<RuleElement> self, List<RuleElement> l) {
        new AndList(new OneOrMore(self), new ZeroOrOne(l))
    }

    static AndList and(Closure<RuleElement> self, String s) {
        new AndList(new OneOrMore(self), new Token(s))
    }

    static AndList and(String self, RuleElement r) {
        new AndList(new Token(self), r)
    }

    static AndList and(String self, List<RuleElement> l) {
        new AndList(new Token(self), new ZeroOrOne(l))
    }

    static AndList and(String self, Closure<RuleElement> c) {
        new AndList(new Token(self), new OneOrMore(c))
    }

    static AndList and(String self, String s) {
        new AndList(new Token(self), new Token(s))
    }

    static AndList and(List<RuleElement> self, RuleElement e) {
        new AndList(new ZeroOrOne(self), e)
    }

    static AndList and(List<RuleElement> self, Closure<RuleElement> c) {
        new AndList(new ZeroOrOne(self), new OneOrMore(c))
    }

    static AndList and(List<RuleElement> self, List<RuleElement> l) {
        new AndList(new ZeroOrOne(self), new ZeroOrOne(l))
    }

    static AndList and(List<RuleElement> self, String s) {
        new AndList(new ZeroOrOne(self), new Token(s))
    }
}
