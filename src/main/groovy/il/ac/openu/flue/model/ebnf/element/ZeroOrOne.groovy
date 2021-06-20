package il.ac.openu.flue.model.ebnf.element

import il.ac.openu.flue.model.rule.Then
import il.ac.openu.flue.model.rule.Expression
import il.ac.openu.flue.model.rule.Optional

/**
 * @author Noam Rotem
 */
class ZeroOrOne implements RuleElement {
    //

    //
    //
    //should be one element

    List<RuleElement> elements = []

    ZeroOrOne(List<?> l) {
        if (l.size() != 1) {
            throw new IllegalStateException("Square brackets must wrap exactly one RuleElement")
        }
        if (l[0] instanceof RuleElement) {
            elements.add(l[0] as RuleElement)
        } else if (l[0] instanceof String) {
            elements.add(new Token(l[0] as String))
        } else {
            throw new IllegalStateException("Square brackets must wrap rule element or string")
        }
    }

    @Override
    String toString() {
        "[" + elements.join(" ") + "]"
    }

    @Override
    Expression expression() {
        Expression e = (elements.size() == 1)? elements[0].expression()
                : new Then(elements.collect {it.expression()})

        new Optional(e)
    }
}
