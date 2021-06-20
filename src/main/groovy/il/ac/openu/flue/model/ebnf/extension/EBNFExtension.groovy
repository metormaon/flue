package il.ac.openu.flue.model.ebnf.extension

import il.ac.openu.flue.model.ebnf.element.*
import il.ac.openu.flue.model.rule.Terminal

import java.util.concurrent.CancellationException

/**
 * @author Noam Rotem
 */
class EBNFExtension {
    static AndList and(Closure<?> self, RuleElement e) {
        new AndList(new OneOrMore(self), e)
    }

    static AndList and(Closure<?> self, Closure<?> c) {
        new AndList(new OneOrMore(self), new OneOrMore(c))
    }

    static AndList and(Closure<?> self, List<?> l) {
        new AndList(new OneOrMore(self), new ZeroOrOne(l))
    }

    static AndList and(Closure<?> self, String s) {
        new AndList(new OneOrMore(self), new Token(s))
    }

    static AndList and(String self, RuleElement r) {
        new AndList(new Token(self), r)
    }

    static AndList and(String self, List<?> l) {
        new AndList(new Token(self), new ZeroOrOne(l))
    }

    static AndList and(String self, Closure<?> c) {
        new AndList(new Token(self), new OneOrMore(c))
    }

    static AndList and(String self, String s) {
        new AndList(new Token(self), new Token(s))
    }

    static AndList and(List<?> self, RuleElement e) {
        new AndList(new ZeroOrOne(self), e)
    }

    static AndList and(List<?> self, Closure<?> c) {
        new AndList(new ZeroOrOne(self), new OneOrMore(c))
    }

    static AndList and(List<?> self, List<?> l) {
        new AndList(new ZeroOrOne(self), new ZeroOrOne(l))
    }

    static AndList and(List<?> self, String s) {
        new AndList(new ZeroOrOne(self), new Token(s))
    }

    static Object asType(String s, Class c) {
        if (c == Terminal) {
            return new Terminal(s)
        }

        //throw new ClassCastException("Custom asType does not support converting to ${c.toString()}")
        return s
    }
}
