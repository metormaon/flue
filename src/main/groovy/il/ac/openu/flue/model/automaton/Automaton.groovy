package il.ac.openu.flue.model.automaton

/**
 * @author Noam Rotem
 */
class Automaton implements Iterable<State> {
    class State {
        final Set<Transition> transitions = new HashSet<>()
        final String id = Integer.toUnsignedString(new Random().nextInt(), 16)

        @Override
        String toString() {
            return id + (transitions? ": " + transitions : "")
        }

        Automaton automaton() {
            Automaton.this
        }
    }

    class Transition {
        final boolean isEpsilon
        final State target
        final Object value

        Transition(State target, Object value = null) {
            isEpsilon = value == null
            this.target = target
            this.value = value
        }

        @Override
        String toString() {
            (isEpsilon? "ε" : value.toString()) + " -> " + target.id
        }
    }

    final Map<String, State> namedStates = new HashMap<>()
    State entryState, finalState

    @Override
    Iterator<State> iterator() {
        if (!entryState || !finalState) {
            throw new IllegalStateException("Missing entry or final states")
        }

        List<State> inspectedStates = [entryState]
        Set<State> discoveredStates = []

        while (inspectedStates) {
            def state = inspectedStates.remove(0)

            if (!discoveredStates.contains(state)) {
                inspectedStates.addAll(state.transitions.collect { it.target })
                discoveredStates << state
            }
        }

        discoveredStates.iterator()
    }

    State named(String stateName) {
        def state = namedStates.get(stateName)
        if (!state) {
            state = new State()
            namedStates[stateName] = state
        }
        state
    }

    Automaton validate() {
        if (!(iterator().collect() as Set).contains(finalState)) {
            throw new IllegalStateException("Validation failed: finalState not accessible from entryState")
        }

        if (finalState.transitions) {
            throw new IllegalStateException("Validation failed: there are transitions from finalState")
        }

        if ((iterator().collect{state -> state.transitions.collect{it.target}}.flatten() as Set)
                .contains(entryState)) {
            throw new IllegalStateException("Validation failed: there are transitions to entryState")
        }

        this
    }

    Automaton startWith(String stateName) {
        startWith(named(stateName))
    }

    Automaton startWith(State state) {
         if (!entryState) {
             entryState = state
             this
         } else {
             throw new IllegalStateException("Automaton already has an entry state")
         }
    }

    Automaton endWith(String stateName) {
        endWith(named(stateName))
    }

    Automaton endWith(State state) {
        if (!finalState) {
            finalState = state
            this
        } else {
            throw new IllegalStateException("Automaton already has a final state")
        }
    }

    Automaton addEpsilonTransition(State from, State to) {
        if (from == finalState || to == entryState) {
            throw new IllegalStateException("Illegal transition")
        }

        from.transitions << new Transition(to)
        this
    }

    Automaton addEpsilonTransition(String from, State to) {
        addEpsilonTransition(named(from), to)
    }

    Automaton addEpsilonTransition(State from, String to) {
        addEpsilonTransition(from, named(to))
    }

    Automaton addEpsilonTransition(String from, String to) {
        addEpsilonTransition(named(from), named(to))
    }

    Automaton addTransition(State from, State to, Object value) {
        if (from == finalState || to == entryState) {
            throw new IllegalStateException("Illegal transition")
        }

        from.transitions << new Transition(to, value)

        this
    }

    Automaton addTransition(String from, State to, Object value) {
        addTransition(named(from), to, value)
    }

    Automaton addTransition(State from, String to, Object value) {
        addTransition(from, named(to), value)
    }

    Automaton addTransition(String from, String to, Object value) {
        addTransition(named(from), named(to), value)
    }

    static Automaton alternation(Automaton a, Automaton b) {
        new Automaton()
                .startWith("x")
                .addEpsilonTransition("x", a.entryState)
                .addEpsilonTransition("x", b.entryState)
                .addEpsilonTransition(a.finalState, "y")
                .addEpsilonTransition(b.finalState, "y")
                .endWith("y")
                .validate()
    }

    static Automaton sequence(Automaton a, Automaton b) {
        new Automaton()
                .startWith(a.entryState)
                .addEpsilonTransition(a.finalState, b.entryState)
                .endWith(b.finalState)
                .validate()
    }

    static Automaton optional(Automaton a) {
        new Automaton()
                .startWith(a.entryState)
                .addEpsilonTransition(a.entryState, a.finalState)
                .endWith(a.finalState)
                .validate()
    }

    static Automaton oneOrMore(Automaton a) {
        new Automaton()
                .startWith("x")
                .addEpsilonTransition("x", a.entryState)
                .addEpsilonTransition(a.finalState, "y")
                .addEpsilonTransition(a.finalState, a.entryState)
                .endWith("y")
                .validate()
    }

    static Automaton zeroOrMore(Automaton a) {
        optional(oneOrMore(a))
    }
}
