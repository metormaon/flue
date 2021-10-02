package il.ac.openu.flue.model.util

import il.ac.openu.flue.JavaEbnf
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

    enum NT implements NonTerminal {A,B,C,D}

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

    @Test
    void testInlining() {
        //Take the Java grammar
        EBNF java = JavaEbnf.grammar()

        //Process a non terminal graph
        Map<NonTerminal, Set<NonTerminal>> graph = java.nonTerminalGraph()

        //Prepare a set of all the non-terminals that were found part of a cycle
        Set<NonTerminal> cyclicNonTerminals = java.cycles(graph).flatten() as Set<NonTerminal>

        //Prepare a set of all the non-terminals that were NOT found in a cycle. These may be replaced by their
        //definition during inlining
        Set<NonTerminal> nonCyclicNonTerminals = graph.keySet() - cyclicNonTerminals

        //Prepare a dependency graph. It's the reversed graph of the nonTerminalGraph. Each node is a non
        //terminal that does not participate in cycles, and the edges from it point to the non-terminals that
        //are dependent on it.
        Map<NonTerminal, Set<NonTerminal>> dependencyGraph =
                nonCyclicNonTerminals.collectEntries {[it, [].toSet()]}

        graph.each {referring, referredSet ->
            referredSet.each { referred ->
                if (referred in nonCyclicNonTerminals) {
                    dependencyGraph[referred] << referring
                }
            }
        }

        //Nodes in the dependence graph that have no edges are entry points of the grammar.
        Set<NonTerminal> entryPoints = dependencyGraph.findAll {!it.value}.keySet()

        //Inlining algorithm:
        //1. Find in graph a node with no edges (fullyInlined) - it means a nonTerminal that depends on no other
        //non-cyclic nonTerminals, and is not an entry point. If there are none - we are done.
        //2. Find in dependencyGraph all the nonTerminals that are based on fullyInlined, and replace their reference
        //to fullyInlined with its definition
        //3. Remove fullyInlined from graph. Remove its rules from rules, unless it's an entry point.

        boolean active

        do {
            active = false
            //Find any non terminal that is not dependent on other non terminals. It needs no further inlining, and
            //may be inlined within its dependents

            Map.Entry<NonTerminal, Set<NonTerminal>> fullyInlinedEntry =
                    graph.find{ k, v -> !v && !(k in entryPoints)}

            //If such fullyInlined exists (otherwise we are done)
            if (fullyInlinedEntry) {
                NonTerminal fullyInlined = fullyInlinedEntry.key

                //Let's have its definition handy. First, we should retrieve the rules that expand it
                List<Rule> expandingRules = java.ruleMap[fullyInlined]

                //If there only one rule that expends fullyInlined, we keep its definition. If there are multi,
                //we should Or all the possible definitions.
                Expression fullyInlinedDefinition = expandingRules.size() == 1? expandingRules[0].definition :
                        new Or(expandingRules.collect{it.definition})

                //For each non terminal that is dependent on the fullyInlined
                dependencyGraph[fullyInlined].each { dependent ->
                    //Grab the rules that expand the dependent, and for each of them
                    java.ruleMap[dependent].each { rule ->
                        //Extract the ExPaths of all the references to fullyInlined within the rule's definition
                        List<ExPath> exPaths = ExPath.match(rule.definition, { Expression e ->
                            e == fullyInlined ? Boolean.TRUE : null
                        })

                        //There may be several references, so for each ExPath
                        exPaths.each { exPath ->
                            //If the rule definition is merely the fullyInlined, replace the definition
                            if (exPath.path.size() == 1) {
                                rule.definition = fullyInlinedDefinition
                            } else { //The fullyInlined is nested within a composite definition
                                //Get the parent of the nonTerminal to be inlined
                                ExPath.PathNode parent = exPath.path[exPath.path.size() - 2]

                                //If the parent is multinary, we need to replace the relevant child
                                if (parent instanceof ExPath.PathMultinaryNode) {
                                    ExPath.PathMultinaryNode multinaryParent = parent as ExPath.PathMultinaryNode
                                    Multinary parentExpression = multinaryParent.expression as Multinary
                                    parentExpression.children[multinaryParent.positionOfNext] = fullyInlinedDefinition
                                } else { //unary parent. Replacing its child
                                    Unary parentExpression = parent.expression as Unary
                                    parentExpression.child = fullyInlinedDefinition
                                }
                            }
                        }
                    }

                    //This dependent is no longer dependent
                    graph[dependent].remove(fullyInlined)
                }

                graph.remove(fullyInlined)

                //Not really needed. But maybe for completion...
                dependencyGraph.remove(fullyInlined)

                java.ruleMap.remove(fullyInlined)
                java.rules.removeAll {it.nonTerminal == fullyInlined}

                active = true
            }
        } while(active)
    }
}
