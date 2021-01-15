package il.ac.openu.flue.processor.ebnf.element

/**
 * @author Noam Rotem
 */
class AndList {
    @Delegate
    List<RuleElement> elements = []

    AndList(RuleElement...e) {
        elements.addAll(e)
        this
    }

    AndList(List<RuleElement> l) {
        elements.addAll(l)
        this
    }

    AndList and(RuleElement e) {
        elements.add(e)
        this
    }

    AndList and(Closure<?> c) {
        Object o = c()

        if (o instanceof RuleElement) {
            elements.add(new ZeroOrMore(o))
        } else if (o instanceof AndList) {
            elements.add(new ZeroOrMore((o as AndList).getElements()))
        } else throw new RuntimeException("Illegal closure type")

        this
    }

    AndList and(List<?> l) {
        if (l.size() != 1) {
            throw new RuntimeException("Illegal list length")
        }

        Object l1 = l[0]

        if (l1 instanceof RuleElement) {
            elements.add(new ZeroOrOne(l1 as RuleElement))
        } else if (l1 instanceof String) {
            elements.add(new ZeroOrOne(new Token(l1 as String)))
        } else if (l1 instanceof AndList) {
            elements.add(new ZeroOrOne((l1 as AndList).getElements()))
        } else throw new RuntimeException("Illegal list element type")

        this
    }

    AndList and(String s) {
        elements.add(new Token(s))
        this
    }
}
