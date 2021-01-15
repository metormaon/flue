package il.ac.openu.flue.model.ebnf

import il.ac.openu.flue.model.ebnf.element.Rule
import il.ac.openu.flue.model.ebnf.element.Variable

/**
 * @author Noam Rotem
 */
class EBNF {
    private static final ThreadLocal<EBNF> context = new ThreadLocal<>()

    private EBNF() {}

    final Set<Rule> rules = []

    Variable root

    static Rule add(Rule r) {
        if (!context.get()) {
            throw new IllegalStateException("Rules must be specified in the context of EBNF grammar. " +
                    "Wrap with ebnf { }.")
        }

        if (context.get().rules.empty) {
            context.get().root = r.variable
        }

        context.get().rules.add(r)
        r
    }

    private static EBNF process(Closure<Rule> c) {
        context.set(new EBNF())
        c()
        EBNF ebnf = context.get()
        context.remove()
        ebnf
    }

    static EBNF ebnf(Closure c) {
        EBNF ebnf = process c
        ebnf
    }

    static EBNF ebnf(Variable v, Closure<Rule> c) {
        EBNF ebnf = process c
        ebnf.root = v
        ebnf
    }

    @Override
    String toString() {
        return rules.collect{it.toString()}.join("\n")
    }

    enum Vars implements Variable {A, B}

    static void main(String[] args) {
        EBNF grammar = ebnf {
            Vars.A >> Vars.B | {Vars.B} & Vars.A
        }

        assert grammar.rules == [] as Set<Rule>
    }
}
