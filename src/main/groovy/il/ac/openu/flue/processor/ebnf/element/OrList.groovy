package il.ac.openu.flue.processor.ebnf.element

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

    OrList or(Closure<RuleElement> c) {
        elements.add(new ZeroOrMore(c()))
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
