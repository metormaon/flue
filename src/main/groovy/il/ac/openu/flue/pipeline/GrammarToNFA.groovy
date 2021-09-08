package il.ac.openu.flue.pipeline

//import il.ac.openu.flue.model.automaton.Automaton
//import il.ac.openu.flue.model.ebnf.element.OrList
//import il.ac.openu.flue.model.ebnf.element.RawRule
//import il.ac.openu.flue.model.ebnf.element.RuleElement
//import il.ac.openu.flue.model.ebnf.element.RuleElementVisitor
//import il.ac.openu.flue.model.ebnf.element.RuleOption
//import il.ac.openu.flue.model.ebnf.element.Token
//import il.ac.openu.flue.model.ebnf.element.Variable
//import il.ac.openu.flue.model.ebnf.element.OneOrMore
//import il.ac.openu.flue.model.ebnf.element.ZeroOrOne

/**
 * @author Noam Rotem
 */
class GrammarToNFA {
//    static Automaton fromRule(RawRule rule) {
//        Automaton automaton = fromRuleOption(rule.options[0])
//
//        if (rule.options.size()>1) {
//            automaton = rule.options[1..-1].inject(automaton) {a, o ->
//                Automaton.alternation(a, fromRuleOption(o))
//            }
//        }
//
//        automaton
//    }
//
//    private static Automaton fromRuleOption(RuleOption ruleOption) {
//        Automaton automaton = fromRuleElement(ruleOption.sequence[0])
//
//        if (ruleOption.sequence.size()>1) {
//            automaton = ruleOption.sequence[1..-1].inject(automaton) {a, e ->
//                Automaton.alternation(a, fromRuleElement(e))
//            }
//        }
//
//        automaton
//    }
//
//    private static Automaton fromRuleElement(RuleElement ruleElement) {
//        AutomatonBuildingVisitor visitor = new AutomatonBuildingVisitor()
//
//        ruleElement.acceptVisitor(visitor)
//
//        visitor.automaton
//    }
//
//    private static class AutomatonBuildingVisitor implements RuleElementVisitor {
//        Automaton automaton
//
//        @Override
//        void visitVariable(Variable variable) {
//            automaton = new Automaton()
//                .startWith("start")
//                .addTransition("start", "end", variable)
//                .endWith("end")
//                .validate()
//        }
//
//        @Override
//        void visitToken(Token token) {
//            automaton = new Automaton()
//                    .startWith("start")
//                    .addTransition("start", "end", token)
//                    .endWith("end")
//                    .validate()
//        }
//
//        @Override
//        void visitOrList(OrList orList) {
//            automaton = fromRuleElement(orList.elements[0])
//
//            if (orList.elements.size()>1) {
//                automaton = orList.elements[1..-1].inject(automaton) {a, e ->
//                    Automaton.sequence(a, fromRuleElement(e))
//                }
//            }
//        }
//
//        @Override
//        void visitOneOrMore(OneOrMore oneOrMore) {
//            automaton = fromRuleElement(oneOrMore.elements[0])
//
//            if (oneOrMore.elements.size()>1) {
//                automaton = oneOrMore.elements[1..-1].inject(automaton) {a, e ->
//                    Automaton.alternation(a, fromRuleElement(e))
//                }
//            }
//
//            automaton = Automaton.oneOrMore(automaton)
//        }
//
//        @Override
//        void visitZeroOrOne(ZeroOrOne zeroOrOne) {
//            automaton = fromRuleElement(zeroOrOne.elements[0])
//
//            if (zeroOrOne.elements.size()>1) {
//                automaton = zeroOrOne.elements[1..-1].inject(automaton) {a, e ->
//                    Automaton.alternation(a, fromRuleElement(e))
//                }
//            }
//
//            automaton = Automaton.optional(automaton)
//        }
//    }
}
