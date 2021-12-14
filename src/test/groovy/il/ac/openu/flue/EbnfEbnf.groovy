package il.ac.openu.flue

import il.ac.openu.flue.model.ebnf.EBNF
import il.ac.openu.flue.model.rule.Variable
import il.ac.openu.flue.model.rule.Terminal

import static il.ac.openu.flue.EbnfEbnf.V.*
import static il.ac.openu.flue.model.ebnf.EBNF.ebnf

/**A crude attempt to enter a BNF from the web: https://golang.org/ref/spec#Notation
 * @author Yossi Gil 
 */
class EbnfEbnf {
    static enum V implements Variable {
        lower, upper, digit, special, character, empty, lhs, option, repetition, sequence, rhs, ebnf_rule,
            ebnf_description, string
    }

    static void main(String[] args) {
        EBNF ebnfGrammar = ebnf {
            lower >> ~"[a-z]"
            upper >> ~"[A-Z]"
            digit >> ~"[0-9]"
            special >> ~"[\\-_\"&’()*+,./:;<=>]"
            character >> lower | upper | digit | special
            string >> "\"" & character & {character} & "\""
            empty >> Terminal.ε
            lhs >> lower & {["_"] & lower}
            option >> "[" & rhs & "]"
//            repetition >> rhs "{" & rhs "}"
            sequence >> empty | {string | lhs | option | repetition}
            rhs >> sequence & { "|" & sequence}
            ebnf_rule >> lhs & ":=" & rhs
            ebnf_description >> {ebnf_rule}
        }

        println("Cycles:")
        println(ebnfGrammar.cycles().join("\n"))
    }
}

/*

EBNF Description: ebnf description
lower ⇐
upper ⇐ A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z
digit ⇐ 0|1|2|3|4|5|6|7|8|9
special ⇐ -| |"|#|&|’|(|)|*|+|,|.|/|:|;|<|=|>
character ⇐ lower | upper | digit | special
empty ⇐
lhs ⇐ lower{[ ]lower}
option ⇐ [ rhs ]
repetition ⇐ { rhs }
sequence ⇐ empty | {character | lhs | option | repetition}
rhs ⇐ sequence{ | sequence}
ebnf rule ⇐ lhs ⇐ rhs
ebnf description ⇐ {enbf rule}

 */