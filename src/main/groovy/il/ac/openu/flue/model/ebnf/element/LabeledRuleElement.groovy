package il.ac.openu.flue.model.ebnf.element

import groovy.transform.SelfType

/**
 * @author Noam Rotem
 */
@SelfType(Enum)
trait LabeledRuleElement extends RuleElement {
    String label = name()

    @Override
    String toString() {
        label
    }
}
