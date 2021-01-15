package il.ac.openu.flue.processor.ebnf.element

import il.ac.openu.flue.processor.ebnf.element.RuleElement

/**
 * @author Noam Rotem
 */
class Token implements RuleElement {
    String regex

    Token(String regex) {
        this.regex = regex
    }

    @Override
    String toString() {
        "\"" + regex + "\""
    }
}
