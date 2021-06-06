package il.ac.openu.flue.model.ebnf

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType
import il.ac.openu.flue.model.ebnf.element.RawRule
import il.ac.openu.flue.model.ebnf.element.Variable
import il.ac.openu.flue.model.rule.Rule

/**
 * @author Noam Rotem
 */
class EBNF {
    private static final ThreadLocal<EBNF> context = new ThreadLocal<>()

    private EBNF() {}

    Variable root
    private Set<RawRule> rawRules = []

    List<Rule> rules
    Map<Variable, Rule> ruleMap

    private void transformRules() {
        rules = rawRules.collect {new Rule(it.variable, it.expression())}
        ruleMap = rules.collectEntries {
            [(it.nonTerminal): it]
        }
    }

    static RawRule add(RawRule r) {
        if (!context.get()) {
            throw new IllegalStateException("Rules must be specified in the context of EBNF grammar. " +
                    "Wrap with ebnf { }.")
        }

        if (context.get().rawRules.empty) {
            context.get().root = r.variable
        }

        context.get().rawRules += r
        r
    }

    private static EBNF process(Closure<RawRule> c) {
        context.set(new EBNF())
        c()
        EBNF ebnf = context.get()
        context.remove()
        ebnf.transformRules()
        ebnf
    }

    static EBNF ebnf(Closure c) {
        EBNF ebnf = process c
        ebnf
    }

    static EBNF ebnf(Variable v, Closure<RawRule> c) {
        EBNF ebnf = process c
        ebnf.root = v
        ebnf
    }

    Rule definitionOf(Variable v) {
        return ruleMap[v]
    }

    Set<Rule> select(@ClosureParams(value = SimpleType, options = "il.ac.openu.flue.model.rule.Rule")
                            Closure<Boolean> ruleFunction) {
        rules.findAll{ruleFunction(it)}.toSet()
    }

    def <T> T query(
            @ClosureParams(value = FromString, options = "T, il.ac.openu.flue.model.rule.Rule")
                    T state, Closure<T> ruleFunction) {
        rules.inject state, ruleFunction
    }
}
