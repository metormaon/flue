package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.ebnf.EBNF

/**
 * @author Noam Rotem
 */
trait Variable extends LabeledRuleElement {
    Rule rightShift(RuleElement e) {
        EBNF.add(new Rule(this, e))
    }

    Rule rightShift(Closure<RuleElement> c) {
        RuleElement result = c()
        EBNF.add(new Rule(this, new ZeroOrMore(result)))
    }

    Rule rightShift(List<RuleElement> l) {
        EBNF.add(new Rule(this, new ZeroOrOne(l[0])))
    }

    Rule rightShift(String s) {
        EBNF.add(new Rule(this, new Token(s)))
    }
}