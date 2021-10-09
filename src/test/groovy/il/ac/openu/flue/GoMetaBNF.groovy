package il.ac.openu.flue

import il.ac.openu.flue.model.ebnf.EBNF
import il.ac.openu.flue.model.rule.NonTerminal

import static il.ac.openu.flue.GoMetaBNF.V.*
import static il.ac.openu.flue.model.ebnf.EBNF.ebnf

/**A crude attempt to enter a BNF from the web: https://golang.org/ref/spec#Notation
 * @author Yossi Gil 
 */
class GoMetaBNF {
    static enum V implements NonTerminal { Production, Identifier, Expression, Alternative, Term, Token, Group, Option,
    Repetition }

    static void main(String[] args) {
        EBNF goMetaBNF = ebnf {
            Production  >> Identifier & "=" & [Expression] & "."
            Expression  >> Alternative & { "|" & Alternative }
            Alternative >> Term & { Term }
            Term        >> Identifier | Token & [ "…" & Token ] | Group | Option | Repetition
            Group       >> "(" & Expression &  ")"
            Option      >> "[" & Expression &  "]"
            Repetition  >> "{" & Expression &  "}"
            Identifier  >> ~"[A-Za-z][A-Za-z0-9_]*"
            Token       >> ~"\"[^\"]+\""
        }

        println("Cycles:")
        println(goMetaBNF.cycles().join("\n"))
    }
}
