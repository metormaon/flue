package il.ac.openu.flue.model.ebnf

import il.ac.openu.flue.model.ebnf.extension.EBNFExtension
import il.ac.openu.flue.model.rule.NonTerminal
import il.ac.openu.flue.model.rule.Or
import il.ac.openu.flue.model.rule.Repeated
import il.ac.openu.flue.model.rule.Optional
import il.ac.openu.flue.model.rule.Rule
import il.ac.openu.flue.model.rule.Terminal
import il.ac.openu.flue.model.rule.Then
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static il.ac.openu.flue.model.ebnf.EBNF.ebnf
import static il.ac.openu.flue.model.ebnf.EBNFTest.V.*
import static il.ac.openu.flue.model.rule.Expression.Visitor
import static il.ac.openu.flue.model.rule.Terminal.ε

/**
 * @author Noam Rotem
 */
class EBNFTest {
    static enum V implements NonTerminal { A, B, C, D, E, F, G }

    static def t(String s) {
        new Terminal(s)
    }

    static def t(String s, boolean pattern) {
        new Terminal(s,pattern)
    }

    @Test
    void testRoot() {
        assert ebnf (A) {
            A >> "x"
        }.root == A

        assert ebnf (B) {
            A >> "x"
        }.root == B

        assert ebnf {
            D >> C & D
            D >> {B}
            A >> B | C
            B >> C
            C >> "W" | [D]
        }.root == A

        shouldFail(IllegalArgumentException) {
            ebnf {
                A >> B | C
                B >> A
            }.root
        }

        assert ebnf {
            A >> B
            B >> C
            D >> C
        }.root == A

        assert ebnf {
            D >> B
            A >> B
            B >> C
            D >> C
        }.root == D

        assert ebnf {
            A >> B
            B >> C
            D >> C
        }.root in ([A, D] as Set<NonTerminal>)
    }

    @Test
    void testNonTerminalGraph() {
        EBNF grammar = ebnf {
            A >> B
            A >> "G"
            B >> "E" | C
            C >> "F"
            C >> A | ε
            D >> {C} & [E & B] & "M" & "L" | ["R"]
            E >> "Q"
        }

        assert grammar.nonTerminalGraph() == [
                (A): [B].toSet(),
                (B): [C].toSet(),
                (C): [A].toSet(),
                (D): [C, E, B].toSet(),
                (E): [].toSet(),
        ]
    }

    @Test
    void testEntryPoints() {
        assert ebnf {
            A >> B
            A >> "G"
            B >> "E" | C
            C >> "F"
            C >> A | ε
            D >> {C} & [E & B] & "M" & "L" | ["R"]
            E >> "Q"
        }.entryPoints() == [D] as Set

        assert ebnf {
            A >> B
            B >> C
            D >> C
        }.entryPoints() == [A, D] as Set

        assert ebnf(A) {
            A >> B
            B >> C
            C >> A
        }.entryPoints() == [] as Set
    }

    @Test
    void testCycles() {
        assert ebnf {
            A >> B
            A >> C
            B >> C
            C >> D
            D >> E
            E >> B
        }.cycles() == [[B, C, D, E]] as Set

        assert ebnf(A) {
            A >> A
            A >> B
            B >> C
            C >> D
            D >> E
            D >> A
            E >> B
        }.cycles() == [[A], [B, C, D, E], [A, B, C, D]] as Set

        assert ebnf(A) {
            A >> B
            B >> C
            C >> A
            D >> E
            E >> F
            F >> B
            F >> E
        }.cycles() == [[A, B, C], [E, F]] as Set

        assert ebnf(A) {
            A >> B
            B >> C
        }.cycles() == [] as Set
    }

    @Test
    void testSimpleRule() {
        EBNF grammar = ebnf {
            A >> B
            A >> ε
            A >> "sd"
            A >> ~"sd"
        }

        assert grammar.rules == [
                new Rule(A, B),
                new Rule(A, t("ε")),
                new Rule(A, t("sd")),
                new Rule(A, t("sd", true))
        ]
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

        assert grammar.rules == [
                new Rule(A, new Then(B, C)),
                new Rule(A, new Then(B, C)),
                new Rule(D, new Then(E, F, G)),
                new Rule(D, new Then(E, F, G)),
                new Rule(D, new Then(E, F, G))
        ]
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

        assert grammar.rules == [new Rule(A, new Or(B, C)),
                                 new Rule(A, new Or(B, C)),
                                 new Rule(A, new Or(B, C, D)),
                                 new Rule(A, new Or(B, C, D)),
                                 new Rule(A, new Or(B, C, D)),
                                 new Rule(A, new Or(B, C, D))]
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

        assert grammar.rules == [new Rule(A, new Or(new Then(B, C), D)),
                                 new Rule(A, new Or(B, new Then(C, D))),
                                 new Rule(A, new Or(B, new Then(C, D), E)),
                                 new Rule(A, new Then(new Or(B, C), new Or(D, E))),
                                 new Rule(A, new Or(new Then(B, C), new Then(D, E))),
                                 new Rule(A, new Then(new Or(B, C), D)),
                                 new Rule(A, new Then(B, new Or(C, D)))]
    }

    @Test
    void testRepeated() {
        use(EBNFExtension) {
            EBNF grammar = ebnf {
                A >> {B}
                A >> {B | C}
                A >> {B & C}
                A >> {B & C | D}
                A >> B & {C}
                A >> {B} & C
                A >> {B} & {C}
                A >> {{B} & C}
                A >> {B}/"abc" & C
                A >> +{B}/"abc"
                A >> +{B} & C
            }

            assert grammar.rules == [
                new Rule(A, new Repeated(B)),
                new Rule(A, new Repeated(new Or(B, C))),
                new Rule(A, new Repeated(new Then(B, C))),
                new Rule(A, new Repeated(new Or(new Then(B, C), D))),
                new Rule(A, new Then(B, new Repeated(C))),
                new Rule(A, new Then(new Repeated(B), C)),
                new Rule(A, new Then(new Repeated(B), new Repeated(C))),
                new Rule(A, new Repeated(new Then(new Repeated(B), C))),
                new Rule(A, new Then(new Repeated(B, new Terminal("abc")), C)),
                new Rule(A, new Repeated(B, new Terminal("abc"), true)),
                new Rule(A, new Then(new Repeated(B, null, true), C))
            ]
        }
    }

    @Test
    void testOptional() {
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

        assert grammar.rules == [new Rule(A, new Optional(B)),
                                 new Rule(A, new Optional(new Or(B, C))),
                                 new Rule(A, new Optional(new Then(B, C))),
                                 new Rule(A, new Optional(new Or(new Then(B, C), D))),
                                 new Rule(A, new Then(B, new Optional(C))),
                                 new Rule(A, new Then(new Optional(B), C)),
                                 new Rule(A, new Then(new Optional(B), new Optional(C))),
                                 new Rule(A, new Optional(new Then(new Optional(B), C)))]
    }

    @Test
    void testAdditionalRulePatterns() {
        assert ebnf {
            A >> { B } & C | { D } & "boolean"
        }.rules == [new Rule(A, new Or(new Then(new Repeated(B), C),
                new Then(new Repeated(D), t("boolean"))))]
    }

    @Test
    void testString() {
        use(EBNFExtension) {
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

                //TODO: include also these:
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
                                     new Rule(A, new Then(B, t("select"))),
                                     new Rule(A, new Or(B, t("select"))),
                                     new Rule(A, new Then(t("select"), B)),
                                     new Rule(A, new Or(t("select"), B)),
                                     new Rule(A, new Then(t("select"), t("*"), t("from"))),
                                     new Rule(A, new Then(B, t("WHERE"), t("[a-b]+"))),
                                     new Rule(A, new Or(new Repeated(t("S")), C)),
                                     new Rule(A, new Then(new Optional(t("R")), t("F"))),
                                     new Rule(A, new Or(C, t("R"))),
                                     new Rule(A, new Then(C, t("R"))),
            ]
        }
    }

    @Test
    void testCombinations() {
        EBNF grammar = ebnf {
            A >> [B & {C}] & "D"
            A >> {B & [C]} & "D"
        }

        assert grammar.rules == [new Rule(A, new Then(new Optional(new Then(B, new Repeated(C))), t("D"))),
                                 new Rule(A, new Then(new Repeated(new Then(B, new Optional(C))), t("D")))]
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
        //Incomplete
        assert ebnf { A >> B }.first() == materialize([A: []])

        EBNF grammar = ebnf {
            A >> B
            A >> "G"
            B >> "E" | C
            C >> "F"
            C >> A | ε
            D >> {C} & [E] & "M" & "L" | ["R"]
            E >> "Q"
        }

        assert grammar.first() == materialize([
                A: ["E", "F", "G", "ε"],
                B: ["E", "F", "G", "ε"],
                C: ["F", "E", "G", "ε"],
                D: ["F", "E", "G", "ε", "Q", "M", "R"],
                E: ["Q"]
        ])
    }

    @Test
    void testNullable() {
        assert ebnf { A >> ε }.nullable() == [(A): true]
        assert ebnf { A >> [ε] }.nullable() == [(A): true]
        assert ebnf { A >> {ε} }.nullable() == [(A): true]
        assert ebnf { A >> "Z" | ε }.nullable() == [(A): true]
        assert ebnf { A >> ε | "Z" }.nullable() == [(A): true]

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

    @Test
    void testFollow() {
        //incomplete
        assert ebnf { A >> B }.follow() == materialize([A: ["ṩ"], B:["ṩ"]])

        assert ebnf {
            A >> B
            B >> "W"
        }.follow() == materialize([
            A: ["ṩ"],
            B: ["ṩ"]
        ])

        assert ebnf {
            A >> B
            B >> "W" | ε
        }.follow() == materialize([
                A: ["ṩ"],
                B: ["ṩ"]
        ])

        assert ebnf {
            A >> B
            B >> C & "w" | D
            C >> ["a"]
            D >> B & "m" & B
        }.follow() == materialize([
                A: ["ṩ"],
                B: ["m", "ṩ"],
                C: ["w"],
                D: ["m", "ṩ"]
        ])

        assert ebnf {
            A >> B & C | {B}
            B >> "w"
            C >> "q" & [B] & [D]
            D >> "s"
        }.follow() == materialize([
                A: ["ṩ"],
                B: ["q", "s", "w", "ṩ"],
                C: ["ṩ"],
                D: ["ṩ"]
        ])


        EBNF grammar = ebnf(A) {
            A >> B & C
            C >> "+" & B & C | ε
            B >> D & E
            E >> "*" & D & E | ε
            D >> "(" & A & ")" | "id"
        }

        assert grammar.follow() == materialize([
                A: ["ṩ", ")"],
                B: ["ṩ", ")", "+"],
                C: ["ṩ", ")"],
                D: ["ṩ", ")", "+", "*"],
                E: ["ṩ", ")", "+"]
        ])
    }

    static Map<NonTerminal, Set<Terminal>> materialize(Map<String, List<String>> expected) {
        expected.collectEntries {
            v , t -> {
                [valueOf(v), t.collect {
                    new Terminal(it)
                }.toSet()]
            }
        } as Map<NonTerminal, Set<Terminal>>
    }
}
