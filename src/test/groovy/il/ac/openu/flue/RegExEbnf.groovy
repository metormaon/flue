package il.ac.openu.flue

import il.ac.openu.flue.model.ebnf.EBNF
import il.ac.openu.flue.model.rule.Rule
import il.ac.openu.flue.model.rule.Variable
import il.ac.openu.flue.pipeline.GrammarToNFA

import static il.ac.openu.flue.RegExEbnf.V.*
import static il.ac.openu.flue.model.ebnf.EBNF.ebnf

/**
 * @author Noam Rotem
 */
class RegExEbnf {
    static enum V implements Variable {Regex, Expression, StartOfStringAnchor, Subexpression, SubexpressionItem, Match,
        Group, Anchor, BackReference, GroupNonCapturingModifier, Quantifier, MatchItem, MatchAnyCharacter,
        MatchCharacterClass, MatchCharacter, CharacterGroup, CharacterClass, CharacterClassFromUnicodeCategory, Char,
        CharacterGroupNegativeModifier, CharacterGroupItem, CharacterRange, CharacterClassAnyWord,
        CharacterClassAnyWordInverted, CharacterClassAnyDecimalDigit, CharacterClassAnyDecimalDigitInverted,
        UnicodeCategoryName, Letters, QuantifierType, LazyModifier, ZeroOrMoreQuantifier, OneOrMoreQuantifier,
        ZeroOrOneQuantifier, RangeQuantifier, RangeQuantifierLowerBound, RangeQuantifierUpperBound, Int,
        AnchorWordBoundary, AnchorNonWordBoundary, AnchorStartOfStringOnly, AnchorEndOfStringOnlyNotNewline,
        AnchorEndOfStringOnly, AnchorPreviousMatchEnd, AnchorEndOfString
    }

    //Grammar is taken from here: https://github.com/kean/Regex/blob/master/grammar.ebnf
    static void main(String[] args) {
        EBNF regex = ebnf(Regex) {

            Regex >> [StartOfStringAnchor] & Expression

            Expression >> Subexpression & ["|" & Expression]

            Subexpression >> {SubexpressionItem}

            SubexpressionItem >> Match
                    | Group
                    | Anchor
                    | BackReference

            Group >> "(" & [GroupNonCapturingModifier] & Expression & ")" & [Quantifier]

            GroupNonCapturingModifier >> "?:"

            Match >> MatchItem & [Quantifier]

            MatchItem >> MatchAnyCharacter
                    | MatchCharacterClass
                    | MatchCharacter

            MatchAnyCharacter >> "."

            MatchCharacterClass >> CharacterGroup
                    | CharacterClass
                    | CharacterClassFromUnicodeCategory

            MatchCharacter >> Char

            CharacterGroup >> "[" & [CharacterGroupNegativeModifier] & {CharacterGroupItem} & "]"

            CharacterGroupNegativeModifier >> "^"

            CharacterGroupItem >> CharacterClass
                    | CharacterClassFromUnicodeCategory
                    | CharacterRange
                    | Char

            CharacterClass >> CharacterClassAnyWord
                    | CharacterClassAnyWordInverted
                    | CharacterClassAnyDecimalDigit
                    | CharacterClassAnyDecimalDigitInverted

            CharacterClassAnyWord >> "\\w"
            CharacterClassAnyWordInverted >> "\\W"
            CharacterClassAnyDecimalDigit >> "\\d"
            CharacterClassAnyDecimalDigitInverted >> "\\D"

            CharacterClassFromUnicodeCategory >> "\\p{" & UnicodeCategoryName & "}"

            UnicodeCategoryName >> Letters

            CharacterRange >> Char & ["-" & Char]

            Quantifier >> QuantifierType & [LazyModifier]

            QuantifierType >> ZeroOrMoreQuantifier
                    | OneOrMoreQuantifier
                    | ZeroOrOneQuantifier
                    | RangeQuantifier

            LazyModifier >> "?"

            ZeroOrMoreQuantifier >> "*"
            OneOrMoreQuantifier >> "+"
            ZeroOrOneQuantifier >> "?"

            RangeQuantifier >> "{" & RangeQuantifierLowerBound & ["," & [RangeQuantifierUpperBound]] & "}"

            RangeQuantifierLowerBound >> Int
            RangeQuantifierUpperBound >> Int

            BackReference >> "\\" & Int

            Anchor >> AnchorWordBoundary
                    | AnchorNonWordBoundary
                    | AnchorStartOfStringOnly
                    | AnchorEndOfStringOnlyNotNewline
                    | AnchorEndOfStringOnly
                    | AnchorPreviousMatchEnd
                    | AnchorEndOfString

            AnchorWordBoundary >> "\\b"

            AnchorNonWordBoundary >> "\\B"

            AnchorStartOfStringOnly >> "\\A"

            AnchorEndOfStringOnlyNotNewline >> "\\z"

            AnchorEndOfStringOnly >> "\\Z"

            AnchorPreviousMatchEnd >> "\\G"

            AnchorEndOfString >> "\$"

            Int >> "[0-9]+"

            Letters >> "[a-zA-Z]+"

            Char >> "#x9" | "#xA" | "#xD" | "[#x20-#xD7FF]" | "[#xE000-#xFFFD]" | "[#x10000-#x10FFFF]"
        }

        regex.rawRules.forEach {
            println(GrammarToNFA.fromRule(it as Rule))
            println(it)
        }
    }
}
