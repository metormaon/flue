package il.ac.openu.flue.processor.ebnf.element

import il.ac.openu.flue.processor.ebnf.element.RuleElement

/**
 * @author Noam Rotem
 */
class ZeroOrMore implements RuleElement {
    List<RuleElement> elements = []

    ZeroOrMore(RuleElement e) {
        elements.add(e)
    }

    ZeroOrMore(List<RuleElement> l) {
        elements.addAll(l)
    }

    @Override
    String toString() {
        "{" + elements.join(" ") + "}"
    }
}
