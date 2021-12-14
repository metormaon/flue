package il.ac.openu.flue

import il.ac.openu.flue.model.ebnf.EBNF
import il.ac.openu.flue.model.rule.Terminal
import il.ac.openu.flue.model.rule.Then
import il.ac.openu.flue.model.rule.Variable

import static il.ac.openu.flue.model.ebnf.EBNF.ebnf

/**A crude attempt to enter a BNF from the web: https://golang.org/ref/spec#Notation
 * @author Yossi Gil 
 */
class FlueEBNF {
    static enum V implements Variable {
        Grammar, Expression, Alternation, Concatenation, Elementary, Symbol
    }
    interface Many<T>  {}
    interface Option<T>{}
    interface Unit {}
    interface Keyword extends Unit {}
    interface Expression {}
    interface Symbol {}




    interface Rule { Symbol head(); Expression body(); }
    interface Grammar extends Many<Rule> { }

    interface Alternation extends Expression, Many<Concatenation> {
        Many<Rule> self();
    }
    interface Concatenation extends Expression, Many<Elementary> {
        Many<Rule> self()
    }
    interface Elmentary {
        
    }

    static <T>  T name(String name, T t) { t }


    static void main(String[] args) {
        EBNF ebnfGrammar = ebnf {
            Grammar >>  +{
                name("rule", name("head:", Symbol) & name("body:" , Expression))
            } / "\n"
            Symbol >> ~"[A-Z][_A-Za-z0-9]*"
            Expression >> Alternation
            Alternation >> +{Concatenation} / "|"
            Concatenation >> +{Elementary} / "&"
            Elementary >> (
                    name("ε:", Terminal.ε)
                |   name("Symbol", Symbol)
                |   name("Literal:", ~"\"[.*]\"")
                |   name("RegularExpression:", "~" & "\"[.*]\"")
                |   name("Nested", "(" & Expression & ")")
                |   name("Option", "[" & Expression & "]")
                |   name("Repetition", name("minimal", [ "+" ]) &  "{" & Expression & "}" & [ "/" & name("By" & Elementary) ])
            )
        }

        println("Cycles:")
        println(ebnfGrammar.cycles().join("\n"))
    }
}