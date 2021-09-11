package il.ac.openu.flue.model.ebnf.extension

import il.ac.openu.flue.model.rule.Expression
import il.ac.openu.flue.model.rule.Optional
import il.ac.openu.flue.model.rule.Or
import il.ac.openu.flue.model.rule.Repeated
import il.ac.openu.flue.model.rule.Terminal
import il.ac.openu.flue.model.rule.Then

/**
 * @author Noam Rotem
 */
class EBNFExtension {
    static Then and(Closure<Expression> self, Expression e) {
        new Then(new Repeated(self()), e)
    }

    static Then and(Closure<Expression> self, Closure<Expression> c) {
        new Then(new Repeated(self()), new Repeated(c()))
    }

    static Then and(Closure<Expression> self, List<Expression> l) {
        new Then(new Repeated(self()), new Optional(l[0]))
    }

    static Then and(Closure<Expression> self, String s) {
        new Then(new Repeated(self()), new Terminal(s))
    }

    static Then and(String self, Expression r) {
        new Then(new Terminal(self), r)
    }

    static Then and(String self, List<Expression> l) {
        new Then(new Terminal(self), new Optional(l[0]))
    }

    static Then and(String self, Closure<Expression> c) {
        new Then(new Terminal(self), new Repeated(c()))
    }

    static Then and(String self, String s) {
        new Then(new Terminal(self), new Terminal(s))
    }

    static Then and(List<Expression> self, Expression e) {
        new Then(new Optional(self[0]), e)
    }

    static Then and(List<Expression> self, Closure<Expression> c) {
        new Then(new Optional(self[0]), new Repeated(c()))
    }

    static Then and(List<Expression> self, List<Expression> l) {
        new Then(new Optional(self[0]), new Optional(l[0]))
    }

    static Then and(List<Expression> self, String s) {
        new Then(new Optional(self[0]), new Terminal(s))
    }

    static Or or(Closure<Expression> self, Expression e) {
        new Or(new Repeated(self()), e)
    }

    static Or or(Closure<Expression> self, Closure<Expression> c) {
        new Or(new Repeated(self()), new Repeated(c()))
    }

    static Or or(Closure<Expression> self, List<Expression> l) {
        new Or(new Repeated(self()), new Optional(l[0]))
    }

    static Or or(Closure<Expression> self, String s) {
        new Or(new Repeated(self()), new Terminal(s))
    }

    static Or or(String self, Expression r) {
        new Or(new Terminal(self), r)
    }

    static Or or(String self, List<Expression> l) {
        new Or(new Terminal(self), new Optional(l[0]))
    }

    static Or or(String self, Closure<Expression> c) {
        new Or(new Terminal(self), new Repeated(c()))
    }

    static Or or(String self, String s) {
        new Or(new Terminal(self), new Terminal(s))
    }

    static Or or(List<Expression> self, Expression e) {
        new Or(new Optional(self[0]), e)
    }

    static Or or(List<Expression> self, Closure<Expression> c) {
        new Or(new Optional(self[0]), new Repeated(c()))
    }

    static Or or(List<Expression> self, List<Expression> l) {
        new Or(new Optional(self[0]), new Optional(l[0]))
    }

    static Or or(List<Expression> self, String s) {
        new Or(new Optional(self[0]), new Terminal(s))
    }

    static Repeated div(Closure<?> c, String s) { new Repeated(c, new Terminal(s)) }

    static Object asType(String s, Class c) {
        if (c == Expression) {
            return new Terminal(s)
        }

        //throw new ClassCastException("Custom asType does not support converting to ${c.toString()}")
        return s
    }
}
