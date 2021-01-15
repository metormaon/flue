package il.ac.openu.flue.model.ebnf.element
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
