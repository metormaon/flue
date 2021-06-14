package il.ac.openu.flue.model.ebnf.element


import il.ac.openu.flue.model.rule.Expression

/**
 * @author Noam Rotem
 */
class RawRule {
    final Variable variable
    RuleElement definition

    RawRule(Variable v, RuleElement e) {
        variable = v
        definition = e
    }

    RawRule and(RuleElement e) {
        definition = definition & e
        this
    }

    RawRule and(Closure<RuleElement> c) {
        definition = definition & c
        this
    }

    RawRule and(List<RuleElement> l) {
        definition = definition & l
        this
    }

    RawRule and(String s) {
        definition = definition & s
        this
    }

    RawRule or(RuleElement e) {
        definition = definition | e
        this
    }

    RawRule or(Closure<RuleElement> c) {
        definition = definition | c
        this
    }

    RawRule or(List<RuleElement> l) {
        definition = definition | l
        this
    }

    RawRule or(String s) {
        definition = definition | s
        this
    }

    @Override
    String toString() {
        variable.toString() + " >> " + definition.expression()
    }

    Expression expression() {
        definition.expression()
    }
}