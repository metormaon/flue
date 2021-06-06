package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.rule.Expression
import il.ac.openu.flue.model.rule.Or

/**
 * @author Noam Rotem
 */
class OrList implements RuleElement {
    List<RuleElement> elements = []

    OrList(RuleElement...e) {
        elements.addAll(e)
    }

    OrList or(RuleElement e) {
        elements.add(e)
        this
    }

    @Override
    Expression expression() {
        new Or(elements.collect{it.expression()})
    }

    OrList or(Closure<RuleElement> c) {
        elements.add(new OneOrMore(c))
        this
    }

    OrList or(List<RuleElement> l) {
        elements.add(new ZeroOrOne(l))
        this
    }

    @Override
    String toString() {
        "(" + elements.collect{it.toString()}.join(" | ") + ")"
    }
}
