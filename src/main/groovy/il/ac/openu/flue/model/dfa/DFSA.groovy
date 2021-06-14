package il.ac.openu.flue.model.dfa

/**
 * Deterministic Finite-State Automaton
 * @author Noam Rotem
 */
@SuppressWarnings('NonAsciiCharacters')
class DFSA {
    private static final ThreadLocal<DFSABuilder> context = new ThreadLocal<>()
    private final int[][] δ
    private final boolean[] F
    private final Map<String, Integer> Σ = [:]
    private final stateCount

    DFSA(int stateCount, Set<String> alphabet) {
        δ = [[-1] * alphabet.size()] * stateCount
        F = new boolean[stateCount]
        alphabet.eachWithIndex{String s, int i -> Σ.put(s, i)}
        this.stateCount = stateCount
    }

    static DFSA dfsa(Closure c) {
        context.set(new DFSABuilder())
        c()
        DFSABuilder dfsa = context.get()
        context.remove()
        dfsa.build()
    }

    DFSA δ(int from, String by, int to) {
        assert from > -1 && from < stateCount
        assert to > -1 && to < stateCount
        assert by != null && by.length() == 1
        assert Σ[by] != null
        assert δ[from][Σ[by]] == -1

        δ[from][Σ[by]] = to
        this
    }

    DFSA δ(int from, Range<String> by, int to) {
        by.each(o -> δ(from, o, to))
        this
    }

    DFSA accepting(int state) {
        assert state > -1 && state < stateCount
        F[state] = true
        this
    }

    static StateSpecification q(int i) {
        new StateSpecification(i)
    }

    static void main(String[] args) {

        //noinspection SpellCheckingInspection
//        new DFSA(10, "abcdefghijklmnop".chars)
//                .δ(0,'a'..'b',3)
//                .δ(1, 'a', 5)

        DFSA dfsa =  dfsa {
            q(0) ---'b'--> q(1)
            q(1) ---('b'..'m')--> q(2)
            q(3) ---['-', '(', ')']--> ~~q(4)
            ~~q(4)
        }
    }

    IntRange getStates() {
        0..(stateCount-1)
    }

    Set<String> getAlphabet() {
        Σ.keySet()
    }

    int target(int from, String letter) {
        0
    }

    static class StateSpecification implements Comparable<StateSpecification>{
        int state
        List<String> letters

        StateSpecification(int state) {
            this.state = state
        }

        StateSpecification minus(String letter) {
            letters = [letter]
            this
        }

        StateSpecification minus(Range<String> range) {
            letters = range.asList()
            this
        }

        StateSpecification minus(ArrayList<String> list) {
            letters = list
            this
        }

        StateSpecification previous(Object s) {
            this
        }

        StateSpecification bitwiseNegate(Object s) {
            context.get().accepting(state)
            this
        }

        @Override
        int compareTo(StateSpecification c) {
            context.get().add(state, letters, c.state)
            0
        }

//        int compareTo(List<StateSpecification> l) {
//            StateSpecification target = l[0]
//            context.get().add(state, letters, target.state, true)
//            0
//        }
    }

    static class DFSABuilder {
        List<Tuple> transitions = []
        Set<Integer> accepting = []

        void add(int from, List<String> by, int to) {
            transitions.add(new Tuple(from, by, to))
        }

        void accepting(int state) {
            accepting.add(state)
        }

        DFSA build() {
            SortedSet<Integer> states = new TreeSet<>()
            SortedSet<String> letters = new TreeSet<>()

            transitions.each {{
                states.add(it[0] as Integer)
                states.add(it[2] as Integer)
                letters.addAll((it[1] as Range<String>).toSet())
            }}

            assert states.first() == 0
            assert states.last() < 1000

            DFSA dfsa = new DFSA(states.last() + 1, letters)

            transitions.each {
                dfsa.δ(it[0] as int, it[1] as Range<String>, it[2] as int)
            }

            accepting.each {dfsa.accepting(it)}

            dfsa
        }
    }

}
