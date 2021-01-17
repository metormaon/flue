package il.ac.openu.flue.model.ebnf.element
/**
 * @author Noam Rotem
 */
class OneOrMore implements RuleElement {
    List<RuleElement> elements = []

    OneOrMore(RuleElement e) {
        elements.add(e)
    }

    OneOrMore(List<RuleElement> l) {
        elements.addAll(l)
    }

    @Override
    String toString() {
        "{" + elements.join(" ") + "}"
    }

    @Override
    void acceptVisitor(RuleElementVisitor visitor) {
        visitor.visitOneOrMore(this)
    }
}
