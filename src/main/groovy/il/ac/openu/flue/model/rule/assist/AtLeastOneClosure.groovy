package il.ac.openu.flue.model.rule.assist

/**
 * @author Noam Rotem
 */
class AtLeastOneClosure extends Closure {
    Closure<?> closure

    AtLeastOneClosure(Closure<?> c) {
        super(c.owner, c.thisObject)
        closure = c
    }

    Object doCall() {
        closure.call()
    }
}
