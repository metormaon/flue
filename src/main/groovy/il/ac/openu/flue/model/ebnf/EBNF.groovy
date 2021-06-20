package il.ac.openu.flue.model.ebnf

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType
import il.ac.openu.flue.model.ebnf.element.RawRule
import il.ac.openu.flue.model.ebnf.element.Variable
import il.ac.openu.flue.model.rule.Expression
import il.ac.openu.flue.model.rule.NonTerminal
import il.ac.openu.flue.model.rule.Optional
import il.ac.openu.flue.model.rule.Or
import il.ac.openu.flue.model.rule.Repeated
import il.ac.openu.flue.model.rule.Rule
import il.ac.openu.flue.model.rule.Terminal
import il.ac.openu.flue.model.rule.Then

import static il.ac.openu.flue.model.rule.Expression.Visitor

/**
 * @author Noam Rotem
 */
class EBNF {
    private static final ThreadLocal<EBNF> context = new ThreadLocal<>()

    private EBNF() {}

    Variable root
    private Set<RawRule> rawRules = []

    List<Rule> rules
    Map<Variable, List<Rule>> ruleMap

    private void transformRules() {
        rules = rawRules.collect {new Rule(it.variable, it.expression())}
        ruleMap = rules.groupBy {
            it.nonTerminal
        }
    }

    static RawRule add(RawRule r) {
        if (!context.get()) {
            throw new IllegalStateException("Rules must be specified in the context of EBNF grammar. " +
                    "Wrap with ebnf { }.")
        }

        if (context.get().rawRules.empty) {
            context.get().root = r.variable
        }

        context.get().rawRules += r
        r
    }

    private static EBNF process(Closure<RawRule> c) {
        context.set(new EBNF())
        c()
        EBNF ebnf = context.get()
        context.remove()
        ebnf.transformRules()
        ebnf
    }

    static EBNF ebnf(Closure c) {
        EBNF ebnf = process c
        ebnf
    }

    static EBNF ebnf(Variable v, Closure<RawRule> c) {
        EBNF ebnf = process c
        ebnf.root = v
        ebnf
    }

    Set<Rule> select(@ClosureParams(value = SimpleType, options = "il.ac.openu.flue.model.rule.Rule")
                            Closure<Boolean> ruleFunction) {
        rules.findAll{ruleFunction(it)}.toSet()
    }

    def <T> T query(
            @ClosureParams(value = FromString, options = "T, il.ac.openu.flue.model.rule.Rule")
                    T state, Closure<T> ruleFunction) {
        rules.inject state, ruleFunction
    }

    Map<Variable, Boolean> nullable() {
        Map<Variable, Boolean> nullable = [:]

        Visitor<Boolean> nullableVisitor = new Visitor<Boolean>() {
            @Override
            Boolean visit(Then then) {
                then.children[0].accept(this)
            }

            @Override
            Boolean visit(Or or) {
                or.children.inject(false) {a, b ->
                    a || b.accept(this)
                }
            }

            @Override
            Boolean visit(Optional optional) {
                true
            }

            @Override
            Boolean visit(Repeated repeated) {
                repeated.child.accept(this)
            }

            @Override
            Boolean visit(NonTerminal nonTerminal) {
                nullable[nonTerminal.variable] ?: false
            }

            @Override
            Boolean visit(Terminal terminal) {
                terminal.terminal == "ε"
            }
        }

        def copyOfNullable

        do {
            copyOfNullable = nullable.clone()

            ruleMap.collect { Variable v, List<Rule> rs ->
                rs.forEach { Rule r ->
                    nullable.merge(v, r.definition.accept(nullableVisitor),
                            (Boolean oldFirst, Boolean newFirst) -> oldFirst || newFirst
                    )
                }
            }
        } while (copyOfNullable != nullable)

        nullable
    }

    Map<Variable, Set<Terminal>> first() {
        Map<Variable, Set<Terminal>> first = [:]

        Visitor<Set<Terminal>> firstVisitor = new Visitor<Set<Terminal>>() {
            @Override
            Set<Terminal> visit(Then then) {
                Set<Terminal> firstOfThen = []

                then.children.find { Expression e ->
                    Set<Terminal> firstOfChild = e.accept(this)
                    firstOfThen += firstOfChild
                    return !firstOfChild.contains(new Terminal("ε"))
                }

                firstOfThen
            }

            @Override
            Set<Terminal> visit(Or or) {
                or.children.inject([].toSet()) {a, b ->
                    a + b.accept(this)
                }
            }

            @Override
            Set<Terminal> visit(Optional optional) {
                optional.child.accept(this) + new Terminal("ε")
            }

            @Override
            Set<Terminal> visit(Repeated repeated) {
                repeated.child.accept(this)
            }

            @Override
            Set<Terminal> visit(NonTerminal nonTerminal) {
                first[nonTerminal.variable] ?: []
            }

            @Override
            Set<Terminal> visit(Terminal terminal) {
                [terminal]
            }
        }

        def copyOfFirst

        do {
            copyOfFirst = first.clone()

            ruleMap.collect { Variable v, List<Rule> rs ->
                rs.forEach { Rule r ->
                    first.merge(v, r.definition.accept(firstVisitor),
                            (Set<Terminal> oldFirst, Set<Terminal> newFirst) -> oldFirst + newFirst
                    )
                }
            }
        } while (copyOfFirst != first)

        first
    }
}
