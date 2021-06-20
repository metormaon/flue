package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.rule.Expression
import il.ac.openu.flue.model.rule.Repeated

/**
 * @author Noam Rotem
 */
class OneOrMore implements RuleElement {
    final RuleElement ruleElement

    OneOrMore(Closure<?> c) {
        if (c() instanceof RuleElement) {
            ruleElement = c() as RuleElement
        } else if (c() instanceof String) {
            ruleElement = new Token((String)c())
        } else {
            //Very strange requirement
            ruleElement = null
            throw new IllegalStateException("Curly brackets must wrap rule element or string")
        }
    }

    @Override
    String toString() {
        "{" + ruleElement.toString() + "}"
    }

    @Override
    Expression expression() {
        new Repeated(ruleElement.expression())
    }
}
