package il.ac.openu.flue.processor.ebnf.element

import il.ac.openu.flue.processor.ebnf.EBNF
import il.ac.openu.flue.processor.ebnf.element.Rule
import il.ac.openu.flue.processor.ebnf.element.RuleElement

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