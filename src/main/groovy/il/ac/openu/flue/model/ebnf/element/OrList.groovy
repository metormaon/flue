package il.ac.openu.flue.model.ebnf.element

/**
 * @author Noam Rotem
 */
class OrList implements RuleElement {
    @Delegate
    List<RuleElement> elements = []

    OrList(RuleElement...e) {
        elements.addAll(e)
    }

    OrList or(RuleElement e) {
        elements.add(e)
        this
    }

    @Override
    void acceptVisitor(RuleElementVisitor visitor) {
        visitor.visitOrList(this)
    }

    OrList or(Closure<RuleElement> c) {
        elements.add(new OneOrMore(c()))
        this
    }

    OrList or(List<RuleElement> l) {
        elements.add(new ZeroOrOne(l[0]))
        this
    }

    @Override
    String toString() {
        "(" + elements.collect{it.toString()}.join(" | ") + ")"
    }
}
