package il.ac.openu.flue.model.ebnf

import il.ac.openu.flue.model.ebnf.element.Variable
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
import static il.ac.openu.flue.model.rule.Expression.Visitor

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
            A >> {"S"} | C
            A >> ["R"] & "F"
//            A >> ("R" | "E")
//            A >> ("R" | C)
//            A >> ("R" | ["E"])
//            A >> ("R" | [C])
//            A >> ("R" | {"E"})
//            A >> ("R" | {C})
//            A >> ("E" | "R")
            A >> (C | "R")
//            A >> (["E"] | "R")
//            A >> ([C] | "R")
//            A >> ({"E"} | "R")
//            A >> ({C} | "R")
//            A >> ("R" & "E")
//            A >> ("R" & C)
//            A >> ("R" & ["E"])
//            A >> ("R" & [C])
//            A >> ("R" & {"E"})
//            A >> ("R" & {C})
//            A >> ("E" & "R")
            A >> (C & "R")
//            A >> (["E"] & "R")
//            A >> ([C] & "R")
//            A >> ({"E"} & "R")
//            A >> ({C} & "R")
        }

        assert grammar.rules == [new Rule(A, t("select")),
                                 new Rule(A, new Then(v(B), t("select"))),
                                 new Rule(A, new Or(v(B), t("select"))),
                                 new Rule(A, new Then(t("select"), v(B))),
                                 new Rule(A, new Or(t("select"), v(B))),
                                 new Rule(A, new Then(t("select"), t("*"), t("from"))),
                                 new Rule(A, new Then(v(B), t("WHERE"), t("[a-b]+"))),
                                 new Rule(A, new Or(new Repeated(t("S")), v(C))),
                                 new Rule(A, new Then(new Optional(t("R")), t("F"))),
                                 new Rule(A, new Or(v(C), t("R"))),
                                 new Rule(A, new Then(v(C), t("R"))),
        ]
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
            Visitor<Boolean> v = new Visitor<Boolean>() {
                @Override
                Boolean visit(Then then) {
                    then.children.inject(false){a,b -> a | b.accept(this)}
                }

                @Override
                Boolean visit(Or or) {
                    or.children.inject(false){a,b -> a | b.accept(this)}
                }

                @Override
                Boolean visit(Optional optional) {
                    optional.child.accept(this)
                }

                @Override
                Boolean visit(Repeated repeated) {
                    repeated.child.accept(this)
                }

                @Override
                Boolean visit(NonTerminal nonTerminal) {
                    false
                }

                @Override
                Boolean visit(Terminal terminal) {
                    true
                }
            }

            return r.definition.accept(v)
        }}

        assert selected.size() == 2
    }

    @Test
    void testFirst() {
        //Empty circular
        assert ebnf { A >> A }.first() == makeComparableToFirst([A: []])

        //Empty wide circle
        assert ebnf { A >> B
            B >> A}.first() == makeComparableToFirst([A: [], B: []])

        //Incomplete
        assert ebnf { A >> B }.first() == makeComparableToFirst([A: []])

        EBNF grammar = ebnf {
            A >> B
            A >> "G"
            B >> "E" | C
            C >> "F"
            C >> A | ε
            D >> {C} & [E] & "M" & "L" | ["R"]
            E >> "Q"
        }

        assert grammar.first() == makeComparableToFirst([
                A: ["E", "F", "G", "ε"],
                B: ["E", "F", "G", "ε"],
                C: ["F", "E", "G", "ε"],
                D: ["F", "E", "G", "ε", "Q", "M", "R"],
                E: ["Q"]
        ])
    }

    static Map<Variable, Set<Terminal>> makeComparableToFirst(Map<String, List<String>> expected) {
        expected.collectEntries {
            v , t -> {
                [valueOf(v), t.collect {
                    new Terminal(it)
                }.toSet()]
            }
        } as Map<Variable, Set<Terminal>>
    }

    @Test
    void testNullable() {
        assert ebnf { A >> ε }.nullable() == [(A): true]
        assert ebnf { A >> [ε] }.nullable() == [(A): true]
        assert ebnf { A >> {ε} }.nullable() == [(A): true]
        assert ebnf { A >> "Z" | ε }.nullable() == [(A): true]
        assert ebnf { A >> ε | "Z" }.nullable() == [(A): true]

        assert ebnf { A >> A }.nullable() == [(A): false]
        assert ebnf { A >> B }.nullable() == [(A): false]
        assert ebnf { A >> "w" }.nullable() == [(A): false]

        EBNF grammar = ebnf {
            A >> B
            A >> "G"
            B >> "E" | C
            C >> "F"
            C >> A | ε
            D >> {C} & [E] & "M" & "L" | ["R"]
            E >> "Q"
        }

        assert grammar.nullable() == [
                (A): true,
                (B): true,
                (C): true,
                (D): true,
                (E): false
        ]
    }

}

//  Please implement also the NULLABLE algorithm. Also as work list.

