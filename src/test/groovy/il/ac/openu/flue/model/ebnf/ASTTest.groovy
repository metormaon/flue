package il.ac.openu.flue.model.ebnf

import il.ac.openu.flue.model.ebnf.element.Variable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static il.ac.openu.flue.model.ebnf.EBNF.ebnf
import static il.ac.openu.flue.model.ebnf.ASTTest.V.*

/**
 * @author Noam Rotem
 */
class ASTTest {
    static enum V implements Variable { A, B, C, D, E, F, G }

    @BeforeEach
    void setUp() {
        AST.ASTClass.ASTField.sequences.clear()
    }

    @Test
    void testInformationalTerminal() {
        assertAST(
                ebnf {
                    B >> "[a-z]"
                }.ast(),
                "class B {String input1}",
        )
    }

    @Test
    void testAtoB() {
        assertAST(
                ebnf {
                    A >> "select" & B
                    C >> B
                    B >> "[a-z]"
                }.ast(),
                "interface A {}",
                "interface C {}",
                "class B implements A, C {String input1}",
        )
    }

    @Test
    void testAtoOptionalB() {
        assertAST(
                ebnf {
                    A >> "select" & [B]
                    B >> "[a-z]"
                }.ast(),
                "class A {Optional<B> b1}",
                "class B {String input1}",
        )
    }

    @Test
    void testAtoRepeatB() {
        assertAST(
                ebnf {
                    A >> "select" & {B}
                    B >> "[a-z]"
                }.ast(),
                "class A {List<B> b1}",
                "class B {String input1}",
        )
    }

    @Test
    void testThenAlone() {
        assertAST(
                ebnf {
                    A >> "jo" & B & "select" & C
                    B >> "[a-z]"
                    C >> "[0-9]+"
                    D >> C
                }.ast(),
        "class A {B b1; C c1}",
        "class B {String input1}",
        "class C implements D {String input2}",
        "interface D {}",
        )
    }

    @Test
    void testThenWithAnotherOr() {
        assertAST(
                ebnf {
                    A >> "+=" & B & "select" & C | D
                    B >> "[a-z]"
                    C >> "[0-9]+"
                    D >> C
                }.ast(),
                "interface A {}",
                "class A1 implements A {B b1; C c1}",
                "class B {String input1}",
                "class C implements D {String input2}",
                "interface D implements A {}"
        )
    }

    @Test
    void testNonInformationalNonTerminals() {
        assertAST(
                ebnf {
                    A >> B & E
                    B >> C & D
                    C >> "hello"
                    D >> "world" & "+"
                    E >> "[a-z]"
                }.ast(),
                "interface A {}",
                "class E implements A {String input1}",
        )
    }

    @Test
    void testSimpleNonInformational() {
        assertAST(
                ebnf {
                    A >> B & C
                    B >> "[a-z]]"
                    C >> "hello"
                }.ast(),
                "interface A {}",
                "class B implements A {String input1}",
        )
    }

    @Test
    void testNonInformational() {
        assertAST(
                ebnf {
                    A >> B | C
                    B >> C | D
                    C >> "hello"
                    D >> "[a-z]]"
                }.ast(),
                "interface A {}",
                "interface B implements A {}",
                "class B implements A {String input1}",
        )
    }

//    @Test
//    void testComplexNonInformational() {
//        assertAST(
//                ebnf {
//                    A >> B & E
//                    B >> C & D
//                    C >> "hello"
//                    D >> "world" & "+"
//                    E >> "[a-z]" | D
//                }.ast(),
//                "interface A {}",
//                "class E implements A {Optional<String input1>}",
//        )
//    }

    static void assertAST(AST ast, String ... images) {
        assert ast.toString().split("\n").collect{it.trim()}.toSet() == (images as List<String>).toSet()
    }
}

