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
 *
 * EBNF is a class for grammar specification in the
 * <a href="https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_form">Extended Backus Naur</a> form.
 * For example, the following code will create an EBNF instance the represents two rules:
 * <pre>
 *
 * EBNF x = ebnf {
 *      A >> B | C
 *      B >> "hello" & [C] & "world"
 *      C >> "."
 * }
 * </pre>
 *
 * The root rule in the above example is detected automatically to be A, since it's the first rule no other rule is
 * is dependent on. It is possible to set the root explicitly:
 * <pre>
 *
 * EBNF x = ebnf(B) {
 *  ...
 * }
 * </pre>
 *
 * A grammar rule is defined as: <i>variable >> expression</i>.<p>
 *
 * <i>variable</i> is an enum member that extends Variable:
 * <pre>
 * enum V implements Variable { A, B, C, D, E, F, G }
 * </pre>
 * <i>expression</i> is made of the following elements and operators:<p>
 * <li>Variable (e.g. A)</li>
 * <li>Terminal (e.g. "." or "class")</li>
 * <li><i>and-then</i> operator: &amp; (e.g. A &amp; ".")</li>
 * <li><i>or</i> operator: | (e.g. A | B)</li>
 * <li><i>optional</i>(zero or one): [ ] (e.g. [A | B])</li>
 * <li><i>repeated</i>(zero or more): { } (e.g. {A & B})</li>
 * <li>parentheses to control precedence: ( ) (e.g. (A | B) & C)</li>
 * <p><p>
 * <p><p>How is the following statement processed by the Groovy interpreter?
 * <pre>
 *
 * EBNF x = ebnf {
 *      A >> B
 *      B >> "hello"
 * }
 * </pre>
 *
 * Explanation: <ol>
 * <li><i>ebnf</i> is a static method of the EBNF class (which is imported statically. </li>
 *
 * <li>The curly brackets wrapping the rules are a closure, passed to ebnf() as a parameter. </li>
 *
 * <li>Each rule is a statement that creates a {@link RawRule} instance. The rule creation moment is when the >>
 * operator is processed: one of {@link Variable}'s rightShift methods is called on the Variable to the left of
 * the >> operator, with part of the right side statement as a parameter.</li>
 *
 * <li>Potentially only part of the right statement is already parsed at that moment, because the precedence of
 * the >> operator is higher than &amp; or |, but that is not an issue. The rest of the right statement elements
 * will add themselves to the RawRule object when processed.</li>
 *
 * <li>The RawRules are created within the scope of the closure, but need to be added to the EBNF instance that
 * represents the grammar, which is not easily accessed within the closure. To solve that challenge, the class uses
 * a {@link ThreadLocal} that holds the EBNF instance while it is populated with rules. The instance is constructed
 * within the scope of the ebnf() static call, then is accessed through the ThreadLocal by the rules closure, which
 * - of course - operates on the same thread. Other threads (=other constructed EBNF instances at the same time) will
 * have their own EBNF instance.</li>
 * </ol><p>
 *
 * The RawRules created while constructing the grammar are suitable for construction time, but not conventient for
 * querying and traversing. The main reason is that to support the neat rule language and defeat precedence issues,
 * for example, Groovy tricks should be used, and the data structure that supports it cannot be immutable and simple.
 * So, once the grammar is fully processed, RawRules are converted into final-form {@link Rule}s.<p>
 * <p>
 * To use EBNF in your file, it is recommended you add static imports to ease the use of your Variables and of
 * EBNF itself:<p><p>
 * import static il.ac.openu.flue.model.ebnf.EBNF.ebnf<p>
 * import static yourPackage.yourClass.V.*<p><p>
 *
 * and you create a Variable enum:<p><p>
 * enum V implements Variable { A, B, C, D, E, F, G }
 *
 * @author Noam Rotem
 */
class EBNF {
    private static final ThreadLocal<EBNF> context = new ThreadLocal<>()
    //End of input terminal. A dollar, but not using the dollar symbol which cannot serve as a constant name
    public static final Terminal ṩ = new Terminal("ṩ")

    //Enpty terminal. Epsilon.
    public static final Terminal ε = new Terminal("ε")

    //No direct construction of grammars
    private EBNF() {}

    //Root rule = start rule
    Variable root

    //Construction-time rules
    private Set<RawRule> rawRules = []

    List<Rule> rules
    Map<Variable, List<Rule>> ruleMap

    /**
     * Grammar factory. Creates an EBNF instance by parsing the rules in the closure. Finds the root by itself.
     */
    static EBNF ebnf(Closure c) {
        EBNF ebnf = process c
        ebnf.root = ebnf.findRoot()
        ebnf
    }

    /**
     * Grammar factory. Creates an EBNF instance by parsing the rules in the closure.
     */
    static EBNF ebnf(Variable v, Closure<RawRule> c) {
        EBNF ebnf = process c
        ebnf.root = v
        ebnf
    }

    /**
     * To be used by the >> operator to add a parsed rule
     */
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

    /**
     *  Creates an EBNF instance from a closure of rules.
     */
    private static EBNF process(Closure<RawRule> c) {
        //A new EBNF instance is set to the ThreadLocal
        context.set(new EBNF())

        //With rule operators redefined in an extension class
        use(EBNFExtension) {
            //Execute the closure (it will parse rules and add them to EBNF instance
            c()

            //Grab the instance from the ThreadLocal
            EBNF ebnf = context.get()

            //Clean the ThreadLocal for this thread. We are done with it
            context.remove()

            //Convert the rules to final-form.
            ebnf.transformRules()

            ebnf
        }
    }

    /**
     * Converting the RawRule instances into final-form Rule objects. All it means is transforming the expression
     * composite into a manageable composite.
     */
    private void transformRules() {
        //Convert expression composites into a simple, useful composite
        rules = rawRules.collect { new Rule(it.variable, it.expression()) }

        //For algorithmic convenience, create also a map from variable to all of its resolving rules
        ruleMap = rules.groupBy {
            it.nonTerminal
        }
    }

    /**
     * A method for automatically finding the root of a grammar, i.e. - it's entry rule
     */
    private Variable findRoot() {
        List<Variable> definedVariables = []
        Set<Variable> referredVariables = []

        //A visitor that goes through expressions and detects referred-to non-terminals
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

        //Visit all rules Find all defined non-terminals and all referred to non-terminals
        rules.forEach(r -> {
            definedVariables += r.nonTerminal
            referredVariables += r.definition.accept(referredVariableVisitor)
        })

        //Calculate root candidates - defined but not referred-to non-terminals
        List<Variable> rootVariables = definedVariables - referredVariables

        if (rootVariables.isEmpty()) {
            throw new IllegalArgumentException("No entry rule was detected")
        } else {
            //Returns an arbitrary root candidate
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

    AST ast() {
        new AST(ruleMap)
    }

    /*
        private boolean isInformational(Expression e) {
        Set<Variable> visited = [] as Set<Variable>

        Expression.Visitor<Boolean> informationalVisitor = new Expression.Visitor<Boolean>() {
            @Override Boolean visit(Then then) { then.children.any {it.accept(this)} }
            @Override Boolean visit(Or or) { true }
            @Override Boolean visit(Optional optional) { true }
            @Override Boolean visit(Repeated repeated) { true }
            @Override Boolean visit(NonTerminal nonTerminal) {
                if (nonTerminal.variable in visited) {
                    true
                } else {
                    visited += nonTerminal.variable
                    ruleMap[nonTerminal.variable].any {it.definition.accept(this)}
                }
            }
            @Override Boolean visit(Terminal terminal) { !(terminal.terminal ==~ /([a-zA-Z]+)|([^a-zA-Z0-9]+)/) }
        }

        e.accept(informationalVisitor)
    }

     */
    Map<Variable, Boolean> informational() {
        Map<Variable, Boolean> nullable = [:]

        ArrayList<?  extends Exception> arr = new ArrayList<>()
        Exception e = null
        arr.add(e)
        true

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
