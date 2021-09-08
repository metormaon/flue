//file:noinspection NonAsciiCharacters
package il.ac.openu.flue.model.dfa

import spock.lang.Specification

import static il.ac.openu.flue.model.dfa.DFSA.*

/**
 * @author Noam Rotem
 */
class DFSATest extends Specification {
    def validateDFSA(DFSA dfsa) {
        /*
            0 --> 'a' --> 1
            1 --> 'b'..'f' --> 2
            1 --> ['m', '-'] --> 3
            2 --> 'a' --> 2
            2 --> '@' --> 3
            3 --> '@' --> 0
            (3)
            (1)
         */

        assert dfsa.states == 0..3
        assert dfsa.alphabet == "abcdefm-@".toSet()
        assert dfsa.target(0,'a') == 1

    }

    def "test δ"() {
//        when:
//            DFSA d = new DFSA(4, "abcdefghij".toSet())
//        then:
//            validateDFSA(d)
    }
}
