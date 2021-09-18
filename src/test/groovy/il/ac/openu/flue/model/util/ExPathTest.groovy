//file:noinspection UnnecessaryQualifiedReference
package il.ac.openu.flue.model.util

import il.ac.openu.flue.model.ebnf.EBNF
import il.ac.openu.flue.model.ebnf.extension.EBNFExtension
import il.ac.openu.flue.model.rule.*
import spock.lang.Specification

import static il.ac.openu.flue.JavaEbnf.V.CaseConstant
import static il.ac.openu.flue.JavaEbnf.V.CaseConstant
import static il.ac.openu.flue.JavaEbnf.V.ElementValuePair
import static il.ac.openu.flue.JavaEbnf.V.ElementValuePair
import static il.ac.openu.flue.JavaEbnf.V.ElementValuePairList
import static il.ac.openu.flue.JavaEbnf.V.Expression
import static il.ac.openu.flue.JavaEbnf.V.Identifier
import static il.ac.openu.flue.JavaEbnf.V.Identifier
import static il.ac.openu.flue.JavaEbnf.V.LambdaParameter
import static il.ac.openu.flue.JavaEbnf.V.LambdaParameter
import static il.ac.openu.flue.JavaEbnf.V.LambdaParameterList
import static il.ac.openu.flue.JavaEbnf.V.ModuleDirective
import static il.ac.openu.flue.JavaEbnf.V.ModuleName
import static il.ac.openu.flue.JavaEbnf.V.ModuleName
import static il.ac.openu.flue.JavaEbnf.V.ModuleName
import static il.ac.openu.flue.JavaEbnf.V.ModuleName
import static il.ac.openu.flue.JavaEbnf.V.ModuleName
import static il.ac.openu.flue.JavaEbnf.V.PackageName
import static il.ac.openu.flue.JavaEbnf.V.PackageName
import static il.ac.openu.flue.JavaEbnf.V.RequiresModifier
import static il.ac.openu.flue.JavaEbnf.V.SwitchLabel
import static il.ac.openu.flue.JavaEbnf.V.TypeArgument
import static il.ac.openu.flue.JavaEbnf.V.TypeArgumentList
import static il.ac.openu.flue.JavaEbnf.V.TypeName
import static il.ac.openu.flue.JavaEbnf.V.TypeName
import static il.ac.openu.flue.JavaEbnf.V.TypeName
import static il.ac.openu.flue.JavaEbnf.V.TypeName
import static il.ac.openu.flue.model.ebnf.EBNF.ebnf
import static il.ac.openu.flue.model.util.ExPath.*
import static il.ac.openu.flue.model.util.ExPathTest.NT.*

/**
 * @author Noam Rotem
 */
class ExPathTest extends Specification {
    enum NT implements NonTerminal {A,B,C}

    def "Test toString"(ExPath exPath, String expectedString) {
        expect:
            exPath.toString() == expectedString

        where:
            exPath                                                      |   expectedString
            ExPath.of(PathNode.of(new Terminal("a")))                   |   "*/'a'"
            ExPath.of(PathNode.of(A))                                   |   "*/A"
            ExPath.of(PathNode.of(new Then(A,B,C),1))                   |   "*/&1"
            ExPath.of(PathNode.of(new Or(A,B,C),1))                     |   "*/|1"
            ExPath.of(PathNode.of(new Optional(new Or(A,B,C))),
                PathNode.of(new Or(A,B,C),1))                           |   "*/[]/|1"
            ExPath.of(PathNode.of(new Repeated(new Or(A,B,C))),
                    PathNode.of(new Then(A,B,C),1))                     |   "*/{}/&1"
    }

    def "test PathVisitor"(Expression e, Closure<Boolean> criterion, String expectedString) {
        given:
            List<ExPath> results = ExPath.match(e, criterion)

        expect:
            results.collect{it.toString()}.join(" ; ") == expectedString

        where:
            e                   |   criterion                                          |   expectedString
            new Terminal("zzz") |   {it.terminal == "a"}                               |   ""
            new Terminal("a")   |   {it.terminal == "a"}                               |   "*/'a'"
            A & "a"             |   {it instanceof Terminal && it.terminal == "a"}     |   "*/&1/'a'"
            "a" & "a"           |   {it instanceof Terminal && it.terminal == "a"}     |   "*/&0/'a' ; */&1/'a'"
            "a" & {B | "a"}     |   {it instanceof Terminal && it.terminal == "a"}     |   "*/&0/'a' ; */&1/{}/|1/'a'"

        "a" & {B | "a"}     |   {
                it instanceof Or && {
                    Or o = it as Or
                    o.children.size() == 2 && o.children[1] instanceof Terminal
                            && (o.children[1] as Terminal).terminal == "a"
                }
            }                                                                          |   "*/&1/{}/`(B)|(\"a\")`"
    }

    def "Test on java rules"() {
        given:
            EBNF language = use (EBNFExtension) {
                ebnf {
                    TypeArgumentList >> TypeArgument & { "," & TypeArgument }

                    ModuleDirective >> "requires" & { RequiresModifier } & ModuleName & ";"
                            | "exports" & PackageName & ["to" & ModuleName & { "," & ModuleName }] & ";"
                            | "opens" & PackageName & ["to" & ModuleName & { "," & ModuleName }] & ";"
                            | "uses" & TypeName & ";"
                            | "provides" & TypeName & "with" & TypeName & { "," & TypeName } & ";"

                    SwitchLabel >> "case" & CaseConstant & { "," & CaseConstant }
                            | "default"

                    LambdaParameterList >> LambdaParameter & { "," & LambdaParameter }
                            | Identifier & { "," & Identifier }

                    A >> (B | C) & { "," & (B | C)}
                }
            }

//            EBNF polishedLanguage = use (EBNFExtension) {
//                ebnf {
//                    TypeArgumentList >> TypeArgument & {TypeArgument}/","
//
//                    ModuleDirective >> "requires" & { RequiresModifier } & ModuleName & ";"
//                            | "exports" & PackageName & ["to" & ModuleName & {ModuleName}/","] & ";"
//                            | "opens" & PackageName & ["to" & ModuleName & {ModuleName}/","] & ";"
//                            | "uses" & TypeName & ";"
//                            | "provides" & TypeName & "with" & TypeName & {TypeName}/"," & ";"
//
//                    SwitchLabel >> "case" & CaseConstant & {CaseConstant}/","
//                            | "default"
//
//                    LambdaParameterList >> LambdaParameter & {LambdaParameter}/","
//                            | Identifier & {Identifier}/","
//
//                    A >> (B | C) & {(B | C)}/","
//                }
//            }

            Closure<Boolean> matchRepeatedWithSeparator = {Expression e ->
                Boolean result = false
                if (e instanceof Then) {
                    e.children.eachWithIndex{ Expression entry, int i ->
                        if (i + 1 < e.children.size() && e.children[i+1] instanceof Repeated) {
                            Expression expression = e.children[i]
                            Repeated repeated = e.children[i+1] as Repeated

                            if (repeated.child instanceof Then) {
                                Then repeatedThen = repeated.child as Then

                                if (repeatedThen.children.size() == 2 &&
                                        repeatedThen.children[0] instanceof Terminal &&
                                        repeatedThen.children[1] == expression) {
                                    result = true
                                }
                            }
                        }
                    }
                }

                result
            }

            Map<Rule,List<ExPath>> findings = language.rules.inject([:]) {map, rule ->
                map << [(rule): ExPath.match(rule.definition, matchRepeatedWithSeparator)]
            } as Map<Rule,List<ExPath>>

        expect:
            Rule r = language.rules[1]

            //Making sure...
            r.nonTerminal == ModuleDirective

            List<ExPath> exPathList = findings[r]
            exPathList.size() == 3
            ExPath ruleExPath = exPathList[2]
            ruleExPath.path.size() == 2
            ruleExPath.path[0].expression == r.definition
            PathMultinaryNode or = ruleExPath.path[0] as PathMultinaryNode
            or.positionOfNext == 4
            ruleExPath.path[1].expression instanceof Then

            findings.toString() == "[" +
                        "TypeArgumentList >> (TypeArgument)&({(\",\")&(TypeArgument)}):" +
                            "[*/`(TypeArgument)&({(\",\")&(TypeArgument)})`], " +

                        "ModuleDirective >> ((\"requires\")&({RequiresModifier})&(ModuleName)&(\";\"))" +
                                    "|((\"exports\")&(PackageName)&([(\"to\")&(ModuleName)&({(\",\")&(ModuleName)})])&(\";\"))" +
                                    "|((\"opens\")&(PackageName)&([(\"to\")&(ModuleName)&({(\",\")&(ModuleName)})])&(\";\"))" +
                                    "|((\"uses\")&(TypeName)&(\";\"))|((\"provides\")&(TypeName)&(\"with\")&(TypeName)&({(\",\")&(TypeName)})&(\";\")):" +
                            "[" +
                                "*/|1/&2/[]/`(\"to\")&(ModuleName)&({(\",\")&(ModuleName)})`, " +
                                "*/|2/&2/[]/`(\"to\")&(ModuleName)&({(\",\")&(ModuleName)})`, " +
                                "*/|4/`(\"provides\")&(TypeName)&(\"with\")&(TypeName)&({(\",\")&(TypeName)})&(\";\")`" +
                            "], " +

                        "SwitchLabel >> ((\"case\")&(CaseConstant)&({(\",\")&(CaseConstant)}))|(\"default\"):" +
                            "[*/|0/`(\"case\")&(CaseConstant)&({(\",\")&(CaseConstant)})`], " +

                        "LambdaParameterList >> ((LambdaParameter)&({(\",\")&(LambdaParameter)}))|((Identifier)&({(\",\")&(Identifier)})):" +
                            "[" +
                                "*/|0/`(LambdaParameter)&({(\",\")&(LambdaParameter)})`, " +
                                "*/|1/`(Identifier)&({(\",\")&(Identifier)})`" +
                            "], " +

                        "A >> ((B)|(C))&({(\",\")&((B)|(C))}):" +
                            "[*/`((B)|(C))&({(\",\")&((B)|(C))})`]]"
    }
}
