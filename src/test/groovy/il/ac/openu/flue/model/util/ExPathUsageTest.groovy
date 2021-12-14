package il.ac.openu.flue.model.util

import il.ac.openu.flue.model.ebnf.EBNF
import il.ac.openu.flue.model.ebnf.extension.EBNFExtension
import il.ac.openu.flue.model.rule.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static il.ac.openu.flue.JavaEbnf.V.*
import static il.ac.openu.flue.model.ebnf.EBNF.ebnf
import static il.ac.openu.flue.model.util.ExPathUsageTest.NT.*

/**
 * @author Noam Rotem
 */
class ExPathUsageTest {
    static class PatternChildInfo {
        int firstChildInPattern
        Expression coreExpression
        Terminal separator
    }

    enum NT implements Variable {A,B,C,D}

    EBNF language
    EBNF transformedLanguage
    Closure<PatternChildInfo> matchRepeatedWithSeparator

    @BeforeEach
    void setUp() {
        use (EBNFExtension) {
            language = use (EBNFExtension) {
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

                    A >> (B | C) & {"," & (B | C)}

                    A >> B & {"," & B} & {":" & B & {"," & B}}

                    A >> B & {"," & B} & C & {":" & C} & D
                }
            }

            transformedLanguage = ebnf {
                TypeArgumentList >> +{TypeArgument}/","

                ModuleDirective >> "requires" & { RequiresModifier } & ModuleName & ";"
                        | "exports" & PackageName & ["to" & +{ModuleName}/","] & ";"
                        | "opens" & PackageName & ["to" & +{ModuleName}/","] & ";"
                        | "uses" & TypeName & ";"
                        | "provides" & TypeName & "with" & +{TypeName}/"," & ";"

                SwitchLabel >> "case" & +{CaseConstant}/","
                        | "default"

                LambdaParameterList >> +{LambdaParameter}/","
                        | +{Identifier}/","

                A >> +{(B | C)}/","

                A >> +{+{B}/","}/":"

                A >> +{B}/"," & +{C}/":" & D
            }
        }

        matchRepeatedWithSeparator = {Expression e ->
            PatternChildInfo result = null

            if (e instanceof Then) {
                e.children.eachWithIndex{ Expression entry, int i ->
                    if (!result && i + 1 < e.children.size() && e.children[i+1] instanceof Repeated) {
                        Expression expression = e.children[i]
                        Repeated repeated = e.children[i+1] as Repeated

                        if (repeated.child instanceof Then) {
                            Then repeatedThen = repeated.child as Then

                            if (repeatedThen.children.size() == 2 &&
                                    repeatedThen.children[0] instanceof Terminal &&
                                    repeatedThen.children[1] == expression) {
                                result = new PatternChildInfo(firstChildInPattern: i,
                                        coreExpression: repeatedThen.children[1],
                                        separator: repeatedThen.children[0] as Terminal
                                )
                            }
                        }
                    }
                }
            }

            result
        }
    }

    @Test
    void testMatch() {
        Map<Rule,List<ExPath>> findings = language.rules.inject([:]) { map, rule ->
            map << [(rule): ExPath.match(rule.definition, matchRepeatedWithSeparator)]
        } as Map<Rule,List<ExPath>>

        Rule r = language.rules[1]

        //Making sure...
        assert r.nonTerminal == ModuleDirective

        List<ExPath> exPathList = findings[r]
        assert exPathList.size() == 3
        ExPath ruleExPath = exPathList[2]
        assert ruleExPath.path.size() == 2
        assert ruleExPath.path[0].expression == r.definition
        ExPath.PathMultinaryNode or = ruleExPath.path[0] as ExPath.PathMultinaryNode
        assert or.positionOfNext == 4
        assert ruleExPath.path[1].expression instanceof Then

        assert findings.toString() == "[" +
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
                "[*/`((B)|(C))&({(\",\")&((B)|(C))})`], " +

                "A >> (B)&({(\",\")&(B)})&({(\":\")&(B)&({(\",\")&(B)})}):" +
                "[" +
                "*/`(B)&({(\",\")&(B)})&({(\":\")&(B)&({(\",\")&(B)})})`, " +
                "*/&2/{}/`(\":\")&(B)&({(\",\")&(B)})`" +
                "], " +

                "A >> (B)&({(\",\")&(B)})&(C)&({(\":\")&(C)})&(D):" +
                "[*/`(B)&({(\",\")&(B)})&(C)&({(\":\")&(C)})&(D)`]" +
        "]"
    }

    @Test
    void testTransform() {
        language.rules.each{ Rule r ->
            boolean active = true

            while(active) {
                List<ExPath> exPaths = ExPath.match(r.definition, matchRepeatedWithSeparator)

                if(exPaths) {
                    ExPath<PatternChildInfo> exPath = exPaths[0]
                    Then then = exPath.path.last().expression as Then
                    PatternChildInfo info = exPath.info as PatternChildInfo

                    Repeated repeated = new Repeated(info.coreExpression, info.separator,
                            true)

                    then.children[info.firstChildInPattern] = repeated

                    then.children.remove(info.firstChildInPattern + 1)

                    //If the Then now has only one child, and the path includes its parent, we can cancel the Then
                    if (then.children.size() == 1 && exPath.path.size() > 1) {
                        ExPath.PathNode parent = exPath.path[exPath.path.size() - 2]

                        if (parent instanceof ExPath.PathMultinaryNode) {
                            ExPath.PathMultinaryNode multinaryParent = parent as ExPath.PathMultinaryNode
                            Multinary parentExpression = multinaryParent.expression as Multinary
                            parentExpression.children[multinaryParent.positionOfNext] = repeated
                        } else { //unary
                            Unary parentExpression = parent.expression as Unary
                            parentExpression.child = repeated
                        }
                    }
                } else active = false

                if (r.definition instanceof Then && (r.definition as Then).children.size() == 1) {
                    r.definition = (r.definition as Then).children[0]
                }
            }
        }

        assert language.rules  == transformedLanguage.rules
    }
}
