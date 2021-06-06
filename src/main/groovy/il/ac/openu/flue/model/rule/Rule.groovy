package il.ac.openu.flue.model.rule

import il.ac.openu.flue.model.ebnf.element.Variable

/**
 * @author Noam Rotem
 */
class Rule {
    final Variable nonTerminal;
    final Expression definition;

    Rule(Variable nonTerminal, Expression definition) {
        this.nonTerminal = nonTerminal
        this.definition = definition
    }

    @Override
    String toString() {
        nonTerminal.label + " >> " + definition
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Rule rule = (Rule) o

        if (definition != rule.definition) return false
        if (nonTerminal != rule.nonTerminal) return false

        return true
    }

    int hashCode() {
        int result
        result = nonTerminal.hashCode()
        result = 31 * result + definition.hashCode()
        return result
    }
}
