package il.ac.openu.flue.model.automaton

import spock.lang.Specification

/**
 * @author Noam Rotem
 */
class AutomatonTest extends Specification {
    def "Entry and final"() {
        when:
            Automaton a = new Automaton()
                    .startWith("x")
                    .addEpsilonTransition("x","y")
                    .endWith("y")

        then:
            assertEqualAutomata(a, [a.entryState, a.finalState], e(a.entryState, a.finalState))
    }

    def "routing to another automaton"() {
        when:
        Automaton a = new Automaton()
                .startWith("x")
                .addEpsilonTransition("x","y")
                .endWith("y")

        Automaton b = new Automaton()
                .startWith("a")
                .addEpsilonTransition("a", a.entryState)
                .addEpsilonTransition(a.finalState, "b")
                .endWith("b")

        then:
            assertEqualAutomata(b, [a.entryState, a.finalState, b.entryState, b.finalState],
                    e(b.entryState, a.entryState),
                    e(a.entryState, a.finalState),
                    e(a.finalState, b.finalState)
            )
    }

    def "using alternation"() {
        when:
            Automaton a = new Automaton()
                    .startWith("x")
                    .addEpsilonTransition("x","y")
                    .endWith("y")

            Automaton b = new Automaton()
                    .startWith("x")
                    .addEpsilonTransition("x","y")
                    .endWith("y")

            Automaton c = Automaton.alternation(a, b)

        then:
            assertEqualAutomata(c, [a.entryState, a.finalState, b.entryState, b.finalState, c.entryState, c.finalState],
                    e(c.entryState, a.entryState),
                    e(a.entryState, a.finalState),
                    e(a.finalState, c.finalState),
                    e(c.entryState, b.entryState),
                    e(b.entryState, b.finalState),
                    e(b.finalState, c.finalState),
            )
    }

    def "using sequence"() {
        when:
        Automaton a = new Automaton()
                .startWith("x")
                .addEpsilonTransition("x","y")
                .endWith("y")

        Automaton b = new Automaton()
                .startWith("x")
                .addEpsilonTransition("x","y")
                .endWith("y")

        Automaton c = Automaton.sequence(a, b)

        then:
            c.entryState == a.entryState
            c.finalState == b.finalState

            assertEqualAutomata(c, [a.entryState, a.finalState, b.entryState, b.finalState],
                    e(a.entryState, a.finalState),
                    e(a.finalState, b.entryState),
                    e(b.entryState, b.finalState)
        )
    }

    def "using optional"() {
        when:
        Automaton a = new Automaton()
                .startWith("x")
                .addTransition("x", "y", "abcd")
                .addEpsilonTransition("y","z")
                .endWith("z")

        Automaton b = Automaton.optional(a)

        then:
            b.entryState == a.entryState
            b.finalState == a.finalState

            assertEqualAutomata(b, [a.entryState, a.finalState, a.namedStates["y"]],
                    e(a.entryState, a.finalState),
                    t(a.entryState, a.namedStates["y"], "abcd"),
                    e(a.namedStates["y"], a.finalState)
            )
    }

    def "using oneOrMore"() {
        when:
            Automaton a = new Automaton()
                    .startWith("x")
                    .addTransition("x", "y", "abcd")
                    .addEpsilonTransition("y","z")
                    .endWith("z")

            Automaton b = Automaton.oneOrMore(a)

        then:
            assertEqualAutomata(b, [a.entryState, a.finalState, a.namedStates["y"], b.entryState, b.finalState],
                    e(b.entryState, a.entryState),
                    t(a.entryState, a.namedStates["y"], "abcd"),
                    e(a.namedStates["y"], a.finalState),
                    e(a.finalState, b.finalState),
                    e(a.finalState, a.entryState)
            )
    }

    def "using zeroOrMore"() {
        when:
        Automaton a = new Automaton()
                .startWith("ax")
                .addTransition("ax", "ay", "abcd")
                .addEpsilonTransition("ay","az")
                .endWith("az")

        Automaton b = Automaton.zeroOrMore(a)

        then:
        assertEqualAutomata(b, [a.entryState, a.finalState, a.namedStates["ay"], b.entryState, b.finalState],
                e(b.entryState, a.entryState),
                t(a.entryState, a.namedStates["ay"], "abcd"),
                e(a.namedStates["ay"], a.finalState),
                e(a.finalState, b.finalState),
                e(a.finalState, a.entryState),
                e(b.entryState, b.finalState)
        )
    }

    //---------------

    TestTransition e(Automaton.State a, Automaton.State b) {
        new TestTransition(from: a, to: b, value: null)
    }

    TestTransition t(Automaton.State a, Automaton.State b, Object value) {
        new TestTransition(from: a, to: b, value: value)
    }

    class TestTransition {
        Automaton.State from
        Automaton.State to
        Object value

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            TestTransition that = (TestTransition) o

            if (from != that.from) return false
            if (to != that.to) return false
            if (value != that.value) return false

            return true
        }

        int hashCode() {
            int result
            result = from.hashCode()
            result = 31 * result + to.hashCode()
            result = 31 * result + (value != null ? value.hashCode() : 0)
            return result
        }

        @Override
        String toString() {
            return "TestTransition{" +
                    "from=" + (innerName(from) ?: from) +
                    ", to=" + (innerName(to) ?: to) +
                    ", value=" + value +
                    '}'
        }

        static String innerName(Automaton.State state) {
            state.automaton().namedStates.find{ it.value == state }?.key
        }
    }

    void assertEqualAutomata(Automaton a, List<Automaton.State> states, TestTransition ... transitions) {
        assert a.collect() as Set == states as Set

        def actual = a.collect {state ->
            //println(state)
            state.transitions.collect {
                //println(it)
                new TestTransition(from: state, to: it.target, value: it.value)
            }
        }.flatten() as Set

        Set<TestTransition> expected = transitions as Set

        assert actual == expected
    }
}
