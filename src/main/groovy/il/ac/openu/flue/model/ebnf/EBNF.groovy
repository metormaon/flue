//file:noinspection NonAsciiCharacters
package il.ac.openu.flue.model.ebnf

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType
import il.ac.openu.flue.model.ebnf.element.RawRule
import il.ac.openu.flue.model.ebnf.element.Variable
import il.ac.openu.flue.model.ebnf.extension.EBNFExtension
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
    public static final Terminal ṩ = new Terminal("ṩ")
    public static final Terminal ε = new Terminal("ε")

    private EBNF() {}

    Variable root
    private Set<RawRule> rawRules = []

    List<Rule> rules
    Map<Variable, List<Rule>> ruleMap

    private void transformRules() {
        rules = rawRules.collect { new Rule(it.variable, it.expression()) }
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
        use(EBNFExtension) {
            c()
            EBNF ebnf = context.get()
            context.remove()
            ebnf.transformRules()
            ebnf
        }
    }

    static EBNF ebnf(Closure c) {
        EBNF ebnf = process c
        ebnf.root = ebnf.findRoot()
        ebnf
    }

    static EBNF ebnf(Variable v, Closure<RawRule> c) {
        EBNF ebnf = process c
        ebnf.root = v
        ebnf
    }

    private Variable findRoot() {
        List<Variable> definedVariables = []
        Set<Variable> referredVariables = []

        Visitor<Set<Variable>> referredVariableVisitor = new Visitor<Set<Variable>>() {
            @Override Set<Variable> visit(Then then) { then.children.inject([].toSet())
                    { s, e -> s + e.accept(this)} }
            @Override Set<Variable> visit(Or or) { or.children.inject([].toSet())
                    { s, e -> s + e.accept(this)} }
            @Override Set<Variable> visit(Optional optional) { optional.child.accept(this) }
            @Override Set<Variable> visit(Repeated repeated) { repeated.child.accept(this) }
            @Override Set<Variable> visit(NonTerminal nonTerminal) { [nonTerminal.variable].toSet() }
            @Override Set<Variable> visit(Terminal terminal) { [].toSet() as Set<Variable> }
        }

        rules.forEach(r -> {
            definedVariables += r.nonTerminal
            referredVariables += r.definition.accept(referredVariableVisitor)
        })

        List<Variable> rootVariables = definedVariables - referredVariables

        if (rootVariables.isEmpty()) {
            throw new IllegalArgumentException("No entry rule was detected")
        } else {
            rootVariables[0]
        }
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

        NullableVisitor nullableVisitor = new NullableVisitor(nullable)

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

        FirstVisitor firstVisitor = new FirstVisitor(first)

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

    Map<Variable, Set<Terminal>> follow(Map<Variable, Set<Terminal>> first = null, Map<Variable,
            Boolean> nullable = null)  {
        if (first == null) {
            first = this.first()
        }

        FirstVisitor firstVisitor = new FirstVisitor(first)

        if (nullable == null) {
            nullable = this.nullable()
        }

        NullableVisitor nullableVisitor = new NullableVisitor(nullable)

        Map<Variable, Set<Terminal>> follow = [:]

        def copyOfFollow

        do {
            copyOfFollow = follow.clone()

            rules.forEach {rule ->
                //Rule #1: S -> $
                if (rule.nonTerminal == root) {
                    follow.merge(root, [ṩ].toSet(),
                            (Set<Terminal> oldFirst, Set<Terminal> newFirst) -> oldFirst + newFirst)
                }

                //Rule #2: A -> αBβ => FOLLOW(B) += FIRST(β)
                Visitor<Set<Variable>> nonTerminalResolver = new Visitor<Set<Variable>>() {
                    @Override
                    Set<Variable> visit(Then then) {
                        //Collect all the children that are NOT nullable
                        List<Expression> nonNullables = then.children.findAll{!it.accept(nullableVisitor)}

                        switch(nonNullables.size()) {
                            //If all the children are nullable, then each one may be resolvable to a variable, while
                            //all the others are null. So try to resolve every child.
                            case 0:
                                return then.children.inject([].toSet(), { set, e ->
                                    set + e.accept(this)
                                })

                            //One non nullable child? Try to resolve it to non terminal, assuming the others are null
                            case 1:
                                return nonNullables[0].accept(this)

                            //More than one non nullable child means that this Then cannot be resolved into one variable
                            default:
                                return []
                        }
                    }

                    @Override Set<Variable> visit(Or or) { or.children.inject([].toSet())
                            {a, b -> a + b.accept(this)} }
                    @Override Set<Variable> visit(Optional optional) { optional.child.accept(this) }
                    @Override Set<Variable> visit(Repeated repeated) { repeated.child.accept(this) }
                    @Override Set<Variable> visit(NonTerminal nonTerminal) { [nonTerminal.variable] }
                    @Override Set<Variable> visit(Terminal terminal) { [] }
                }

                Visitor<Void> sequenceVisitor = new Visitor<Void>() {
                    @Override
                    Void visit(Then then) {
                        then.children.init().eachWithIndex{ Expression e, int i -> {
                            Set<Variable> childNonTerminalResolution = e.accept(nonTerminalResolver)

                            childNonTerminalResolution.forEach{Variable v ->
                                Expression restOfSequence = new Then(then.children.drop(i+1))

                                follow.merge(v, restOfSequence.accept(firstVisitor) - [ε],
                                        (Set<Terminal> oldFirst, Set<Terminal> newFirst) ->
                                                oldFirst + newFirst)
                            }
                        }}
                        null
                    }

                    @Override Void visit(Or or) { or.children.forEach{e -> e.accept(this)}
                        null }

                    @Override Void visit(Optional optional) { optional.child.accept(this)
                        null }

                    @Override
                    Void visit(Repeated repeated) {
                        repeated.child.accept(this)
                        new Then(repeated.child, repeated.child).accept(this)
                        null
                    }

                    //NonTerminal and Terminal cannot be resolved into αBβ sequences, so we use the base implementation
                    //that returns null
                }

                rule.definition.accept(sequenceVisitor)

                //Rule #3: A -> αB || A -> αBβ && NULLABLE(β) => FOLLOW(B) += FOLLOW(A)
                Visitor<Set<Variable>> nonTerminalsAtEndVisitor = new Visitor<Set<Variable>>() {
                    @Override
                    Set<Variable> visit(Then then) {
                        Set<Variable> nonTerminalsAtEnd = then.children.last().accept(this)

                        if (then.children.last().accept(nullableVisitor)) {
                            if (then.children.size() > 2) {
                                nonTerminalsAtEnd += new Then(then.children.take(then.children.size()-1)).accept(this)
                            } else {
                                nonTerminalsAtEnd += then.children[0].accept(this)
                            }
                        }

                        nonTerminalsAtEnd
                    }

                    @Override Set<Variable> visit(Or or) { or.children.inject([].toSet())
                            {a, b -> a + b.accept(this)} }
                    @Override Set<Variable> visit(Optional optional) { optional.child.accept(this) }
                    @Override Set<Variable> visit(Repeated repeated) { repeated.child.accept(this) }
                    @Override Set<Variable> visit(NonTerminal nonTerminal) { [nonTerminal.variable] }
                    @Override Set<Variable> visit(Terminal terminal) { [] }
                }

                Set<Variable> nonTerminalsAtEnd = rule.definition.accept(nonTerminalsAtEndVisitor)

                nonTerminalsAtEnd.forEach { endingNonTerminal ->
                    follow.merge(endingNonTerminal, follow.get(rule.nonTerminal, new HashSet<Terminal>()) - [ε],
                            (Set<Terminal> oldFirst, Set<Terminal> newFirst) -> oldFirst + newFirst)
                }
            }

        } while (copyOfFollow != follow)

//        follow.each {
//            it.value -= ε
//        }

        follow
    }

    class FirstVisitor extends Visitor<Set<Terminal>> {
        private final Map<Variable, Set<Terminal>> first

        FirstVisitor(Map<Variable, Set<Terminal>> first) {
            this.first = first
        }

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

    class NullableVisitor extends Visitor<Boolean> {
        private final Map<Variable, Boolean> nullable

        NullableVisitor(Map<Variable, Boolean> nullable) {
            this.nullable = nullable
        }

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
}
