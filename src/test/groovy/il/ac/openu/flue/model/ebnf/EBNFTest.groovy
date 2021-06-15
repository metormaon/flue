package il.ac.openu.flue.model.ebnf

import il.ac.openu.flue.model.ebnf.element.Token
import il.ac.openu.flue.model.ebnf.element.Variable
import il.ac.openu.flue.model.rule.ExpressionTraverser
import il.ac.openu.flue.model.rule.ExpressionTraverserBase
import il.ac.openu.flue.model.rule.ExpressionVisitor
import il.ac.openu.flue.model.rule.ExpressionVisitorBase
import il.ac.openu.flue.model.rule.NonTerminal
import il.ac.openu.flue.model.rule.Or
import il.ac.openu.flue.model.rule.Repeated
import il.ac.openu.flue.model.rule.Optional
import il.ac.openu.flue.model.rule.Rule
import il.ac.openu.flue.model.rule.Terminal
import il.ac.openu.flue.model.rule.Then
import org.junit.jupiter.api.Test

import static il.ac.openu.flue.model.ebnf.EBNF.ebnf
import static il.ac.openu.flue.model.ebnf.EBNFTest.V.*
import static il.ac.openu.flue.model.ebnf.element.Token.*

/**
 * @author Noam Rotem
 */
class EBNFTest {
    static enum V implements Variable { A, B, C, D, E, F, G }

    static def v(Variable v) {
        new NonTerminal(v)
    }

    static def t(String s) {
        new Terminal(s)
    }

    @Test
    void testSimpleRule() {
        EBNF grammar = ebnf {
            A >> B
            A >> ε
            A >> "sd"
        }

        assert grammar.rules == [new Rule(A, v(B)), new Rule(A, t("ε")), new Rule(A, t("sd"))]
    }

    @Test
    void testThen() {
        EBNF grammar = ebnf {
            A >> B & C
            A >> (B & C)
            D >> E & F & G
            D >> (E & F) & G
            D >> E & (F & G)
        }

        assert grammar.rules == [new Rule(A, new Then(v(B), v(C))),
                                 new Rule(A, new Then(v(B), v(C))),
                                 new Rule(D, new Then(v(E), v(F), v(G))),
                                 new Rule(D, new Then(v(E), v(F), v(G))),
                                 new Rule(D, new Then(v(E), v(F), v(G)))]
    }

    @Test
    void testOr() {
        EBNF grammar = ebnf {
            A >> B | C
            A >> (B | C)
            A >> B | C | D
            A >> (B | C) | D
            A >> B | (C | D)
            A >> (B | C | D)
        }

        assert grammar.rules == [new Rule(A, new Or(v(B), v(C))),
                                 new Rule(A, new Or(v(B), v(C))),
                                 new Rule(A, new Or(v(B), v(C), v(D))),
                                 new Rule(A, new Or(v(B), v(C), v(D))),
                                 new Rule(A, new Or(v(B), v(C), v(D))),
                                 new Rule(A, new Or(v(B), v(C), v(D)))]
    }

    @Test
    void testMixedOrAndThen() {
        EBNF grammar = ebnf {
            A >> B & C | D
            A >> B | C & D
            A >> B | C & D | E
            A >> (B | C) & (D | E)
            A >> B & C | D & E
            A >> (B | C) & D
            A >> B & (C | D)
        }

        assert grammar.rules == [new Rule(A, new Or(new Then(v(B), v(C)), v(D))),
                                 new Rule(A, new Or(v(B), new Then(v(C), v(D)))),
                                 new Rule(A, new Or(v(B), new Then(v(C), v(D)), v(E))),
                                 new Rule(A, new Then(new Or(v(B), v(C)), new Or(v(D), v(E)))),
                                 new Rule(A, new Or(new Then(v(B), v(C)), new Then(v(D), v(E)))),
                                 new Rule(A, new Then(new Or(v(B), v(C)), v(D))),
                                 new Rule(A, new Then(v(B), new Or(v(C), v(D))))]
    }

    @Test
    void testOneOrMore() {
        EBNF grammar = ebnf {
            A >> {B}
            A >> {B | C}
            A >> {B & C}
            A >> {B & C | D}
            A >> B & {C}
            A >> {B} & C
            A >> {B} & {C}
            A >> {{B} & C}
        }

        assert grammar.rules == [new Rule(A, new Repeated(v(B))),
                                 new Rule(A, new Repeated(new Or(v(B), v(C)))),
                                 new Rule(A, new Repeated(new Then(v(B), v(C)))),
                                 new Rule(A, new Repeated(new Or(new Then(v(B), v(C)), v(D)))),
                                 new Rule(A, new Then(v(B), new Repeated(v(C)))),
                                 new Rule(A, new Then(new Repeated(v(B)), v(C))),
                                 new Rule(A, new Then(new Repeated(v(B)), new Repeated(v(C)))),
                                 new Rule(A, new Repeated(new Then(new Repeated(v(B)), v(C))))]
    }

    @Test
    void testZeroOrOne() {
        EBNF grammar = ebnf {
            A >> [B]
            A >> [B | C]
            A >> [B & C]
            A >> [B & C | D]
            A >> B & [C]
            A >> [B] & C
            A >> [B] & [C]
            A >> [[B] & C]
        }

        assert grammar.rules == [new Rule(A, new Optional(v(B))),
                                 new Rule(A, new Optional(new Or(v(B), v(C)))),
                                 new Rule(A, new Optional(new Then(v(B), v(C)))),
                                 new Rule(A, new Optional(new Or(new Then(v(B), v(C)), v(D)))),
                                 new Rule(A, new Then(v(B), new Optional(v(C)))),
                                 new Rule(A, new Then(new Optional(v(B)), v(C))),
                                 new Rule(A, new Then(new Optional(v(B)), new Optional(v(C)))),
                                 new Rule(A, new Optional(new Then(new Optional(v(B)), v(C))))]
    }

    @Test
    void testString() {
        EBNF grammar = ebnf {
            A >> "select"
            A >> B & "select"
            A >> B | "select"
            A >> "select" & B
            A >> "select" | B
            A >> "select" & "*" & "from"
            A >> B & "WHERE" & "[a-b]+"
        }

        assert grammar.rules == [new Rule(A, t("select")),
                                 new Rule(A, new Then(v(B), t("select"))),
                                 new Rule(A, new Or(v(B), t("select"))),
                                 new Rule(A, new Then(t("select"), v(B))),
                                 new Rule(A, new Or(t("select"), v(B))),
                                 new Rule(A, new Then(t("select"), t("*"), t("from"))),
                                 new Rule(A, new Then(v(B), t("WHERE"), t("[a-b]+")))]
    }

    @Test
    void testCombinations() {
        EBNF grammar = ebnf {
            A >> [B & {C}] & "D"
            A >> {B & [C]} & "D"
        }

        assert grammar.rules == [new Rule(A, new Then(new Optional(new Then(v(B), new Repeated(v(C)))), t("D"))),
                                 new Rule(A, new Then(new Repeated(new Then(v(B), new Optional(v(C)))), t("D")))]
    }

    @Test
    void testSelect() {
        EBNF grammar = ebnf {
            A >> B
            B >> "E" | C
            C >> "F"
        }

        //Select rules with tokens
        Set<Rule> selected = grammar.select {r -> {
            ExpressionVisitor v = new ExpressionVisitorBase(){
                boolean terminalFound

                @Override
                void visit(Terminal t) {
                    terminalFound = true
                }
            }

            r.definition.acceptVisitor(v)

            return v.terminalFound
        }}

        assert selected.size() == 2
    }

    @Test
    void testQuery() {
        EBNF grammar = ebnf {
            A >> B
            B >> "E" | C
            C >> "F"
            D >> {C} & [A]
        }

        //Calculate First closure
        Map<Variable, Set<Terminal>> first = new HashMap<>();

       

        // Please implement also the NULLABLE algorithm. Also as work list.

        grammar.rules.forEach {
          // This is a great algorithm for doing manipulation on regular expressions. Recursion works 
          // these very well. There are such algorithms in the literature
            ExpressionTraverser<Set<Terminal>> traverser = new ExpressionTraverserBase<Set<Terminal>>() {
                Set<NonTerminal> traversedNonTerminals = []

                @Override
                Set<Terminal> traverse(NonTerminal nonTerminal, Set<Terminal> state) {
                    if (!traversedNonTerminals.contains(nonTerminal)) {
                        traversedNonTerminals.add(nonTerminal)
                        Rule rule = grammar.ruleMap.get(nonTerminal.variable)

                        if (rule != null) {
                            state.addAll(rule.definition.acceptTraverser(this, state))
                        }
                    }

                    state
                }

                @Override
                Set<Terminal> traverse(Terminal terminal, Set<Terminal> state) {
                    state + terminal
                }

                @Override
                Set<Terminal> traverse(Then then, Set<Terminal> state) {
                    state.addAll(then.children[0].acceptTraverser(this, state))
                    state
                }

                @Override
                Set<Terminal> traverse(Or or, Set<Terminal> state) {
                    or.children.forEach(c -> state.addAll(c.acceptTraverser(this, state)))
                    state
                }

                @Override
                Set<Terminal> traverse(Optional optional, Set<Terminal> state) {
                    optional.child.acceptTraverser(this, state)
                }

                @Override
                Set<Terminal> traverse(Repeated repeated, Set<Terminal> state) {
                    repeated.child.acceptTraverser(this, state)
                }
            }

            first.put(it.nonTerminal, it.definition.acceptTraverser(traverser, new HashSet<Terminal>()))
        }

        assert first == new HashMap<Variable, Set<Terminal>>(){{
            put(A,new HashSet<Terminal>(){{
                add(new Terminal("E"))
                add(new Terminal("F"))
            }})
            put(B,new HashSet<Terminal>(){{
                add(new Terminal("E"))
                add(new Terminal("F"))
            }})
            put(C,new HashSet<Terminal>(){{
                add(new Terminal("F"))
            }})
            put(D,new HashSet<Terminal>(){{
                add(new Terminal("F"))
            }})
        }}
    }
}
