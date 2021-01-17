package il.ac.openu.flue.model.ebnf.element

/**
 * @author Noam Rotem
 */
interface RuleElementVisitor {
    void visitVariable(Variable variable)
    void visitToken(Token token)
    void visitOrList(OrList orList)
    void visitOneOrMore(OneOrMore zeroOrMore)
    void visitZeroOrOne(ZeroOrOne zeroOrOne)
}