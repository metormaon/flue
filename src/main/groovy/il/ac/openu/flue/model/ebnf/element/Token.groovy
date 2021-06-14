package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.rule.Expression
import il.ac.openu.flue.model.rule.Terminal

/**
 * @author Noam Rotem
 */
class Token implements RuleElement {
    @SuppressWarnings('NonAsciiCharacters')
    public static final Token ε = new Token("ε")

    String regex

    Token(String regex) {
        this.regex = regex
    }

    @Override
    String toString() {
        "\"" + regex + "\""
    }

    @Override
    Expression expression() {
        new Terminal(regex)
    }
}

