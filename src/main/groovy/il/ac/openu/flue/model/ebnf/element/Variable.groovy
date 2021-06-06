package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.ebnf.EBNF
import il.ac.openu.flue.model.rule.Expression
import il.ac.openu.flue.model.rule.NonTerminal

/**
 * @author Noam Rotem
 */
trait Variable extends LabeledRuleElement {
    RawRule rightShift(RuleElement e) {
        EBNF.add(new RawRule(this, e))
    }

    RawRule rightShift(Closure<RuleElement> c) {
        EBNF.add(new RawRule(this, new OneOrMore(c)))
    }

    RawRule rightShift(List<RuleElement> l) {
        EBNF.add(new RawRule(this, new ZeroOrOne(l)))
    }

    RawRule rightShift(String s) {
        EBNF.add(new RawRule(this, new Token(s)))
    }

    @Override
    Expression expression() {
        new NonTerminal(this)
    }
}