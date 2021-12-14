package il.ac.openu.flue.model.rule

/**
 * @author Noam Rotem
 */
class Rule {
    Variable nonTerminal;
    Expression definition;

    Rule(Variable nonTerminal, Expression definition) {
        this.nonTerminal = nonTerminal
        this.definition = definition
    }

    Rule or(Expression e) { definition |= e; this }
    Rule or(Closure<?> c) { definition |= c; this }
    Rule or(List<?> l) { definition |= l; this }
    Rule or(String s) { definition |= s; this }

    Rule and(Expression e) { definition &= e; this }
    Rule and(Closure<?> c) { definition &= c; this }
    Rule and(List<?> l) { definition &= l; this }
    Rule and(String s) { definition &= s; this }

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
