//file:noinspection UnnecessaryQualifiedReference
package il.ac.openu.flue.model.util


import il.ac.openu.flue.model.rule.*
import spock.lang.Specification

import static il.ac.openu.flue.model.util.ExPath.PathNode
import static il.ac.openu.flue.model.util.ExPathTest.NT.*
/**
 * @author Noam Rotem
 */
class ExPathTest extends Specification {
    enum NT implements NonTerminal {
        A, B, C, D
    }

    def "Test toString"(ExPath exPath, String expectedString) {
        expect:
        exPath.toString() == expectedString

        where:
        exPath                                       | expectedString
        ExPath.of(PathNode.of(new Terminal("a")))    | "*/'a'"
        ExPath.of(PathNode.of(A))                    | "*/A"
        ExPath.of(PathNode.of(new Then(A, B, C), 1)) | "*/&1"
        ExPath.of(PathNode.of(new Or(A, B, C), 1))   | "*/|1"
        ExPath.of(PathNode.of(new Optional(new Or(A, B, C))),
                PathNode.of(new Or(A, B, C), 1))     | "*/[]/|1"
        ExPath.of(PathNode.of(new Repeated(new Or(A, B, C))),
                PathNode.of(new Then(A, B, C), 1))   | "*/{}/&1"
    }

    def "test PathVisitor"(Expression e, Closure<Boolean> criterion, String expectedString) {
        given:
        List<ExPath> results = ExPath.match(e, criterion)

        expect:
        results.collect { it.toString() }.join(" ; ") == expectedString

        where:
        e                   | criterion                                        | expectedString
        new Terminal("zzz") | { it.terminal == "a" }                           | ""
        new Terminal("a")   | { it.terminal == "a" }                           | "*/'a'"
        A & "a"             | { it instanceof Terminal && it.terminal == "a" } | "*/&1/'a'"
        "a" & "a"           | { it instanceof Terminal && it.terminal == "a" } | "*/&0/'a' ; */&1/'a'"
        "a" & { B | "a" }   | { it instanceof Terminal && it.terminal == "a" } | "*/&0/'a' ; */&1/{}/|1/'a'"

        "a" & { B | "a" }   | {
            it instanceof Or && {
                Or o = it as Or
                o.children.size() == 2 && o.children[1] instanceof Terminal
                        && (o.children[1] as Terminal).terminal == "a"
            }
        }                                                                      | "*/&1/{}/`(B)|(\"a\")`"
    }
}
