package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.rule.Then
import il.ac.openu.flue.model.rule.Expression

/**
 * @author Noam Rotem
 */
class RuleOption {
    @Delegate
    List<RuleElement> sequence = []

    RuleOption() {}

    RuleOption(RuleElement e) {
        sequence.add(e)
    }

    RuleOption(AndList l) {
        sequence.addAll(l)
    }

    @Override
    String toString() {
        sequence.collect{it.toString()}.join(" ")
    }

    Expression expression() {
        if (sequence.size() == 1) {
            sequence.get(0).expression()
        } else {
            new Then(sequence.collect { it.expression() })
        }
    }
}
