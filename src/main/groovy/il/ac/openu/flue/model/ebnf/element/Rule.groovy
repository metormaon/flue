package il.ac.openu.flue.model.ebnf.element
/**
 * @author Noam Rotem
 */
class Rule {
    final Variable variable
    private final List<RuleOption> options = []

    Rule(Variable v, RuleElement e) {
        variable = v

        if (options.empty) {
            options.add(new RuleOption())
        }

        options.last().add(e)

        this
    }

    Rule and(OrList l) {
        options.last().add(l)
        this
    }

    Rule and(RuleElement e) {
        options.last().add(e)
        this
    }

    Rule and(Closure<?> c) {
        Object o = c()

        if (o instanceof RuleElement) {
            options.last().add(new ZeroOrMore(o))
        } else if (o instanceof AndList) {
            options.last().add(new ZeroOrMore((o as AndList).getElements()))
        } else throw new RuntimeException("Illegal closure type")

        this
    }

    Rule and(List<?> l) {
        if (l.size() != 1) {
            throw new RuntimeException("Illegal list length")
        }

        Object l1 = l[0]

        if (l1 instanceof RuleElement) {
            options.last().add(new ZeroOrOne(l1 as RuleElement))
        } else if (l1 instanceof String) {
            options.last().add(new ZeroOrOne(new Token(l1 as String)))
        } else if (l1 instanceof AndList) {
            options.last().add(new ZeroOrOne((l1 as AndList).getElements()))
        } else throw new RuntimeException("Illegal list element type")

        this
    }

    Rule and(String s) {
        options.last().add(new Token(s))
        this
    }

    Rule or(RuleElement e) {
        options.add(new RuleOption(e))
        this
    }

    Rule or(AndList l) {
        options.add(new RuleOption(l))
        this
    }

    Rule or(Closure<RuleElement> c) {
        options.add(new RuleOption(new ZeroOrMore(c())))
        this
    }

    Rule or(List<RuleElement> l) {
        options.add(new RuleOption(new ZeroOrOne(l[0])))
        this
    }

    Rule or(String s) {
        options.add(new RuleOption(new Token(s)))
        this
    }

    @Override
    String toString() {
        variable.toString() + ":\n" + options.collect{"  " + it.toString()}.join("\n") + "\n"
    }
}
