package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.rule.Then
import il.ac.openu.flue.model.rule.Expression
import il.ac.openu.flue.model.rule.Repeated

/**
 * @author Noam Rotem
 */
class OneOrMore implements RuleElement {
    final RuleElement ruleElement

    OneOrMore(Closure<RuleElement> c) {
        ruleElement = c()
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
