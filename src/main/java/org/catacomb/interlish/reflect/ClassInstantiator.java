package org.catacomb.interlish.reflect;

import java.lang.reflect.Constructor;

import org.catacomb.be.Instantiator;
import org.catacomb.report.E;


// all non-trivial instantiation (ie not primitive types) should go thrueh here in the end ??? REFAC
public class ClassInstantiator implements Instantiator {




    public Object newInstance(String cnm) {
        Object ret = null;
        Class<?> c = null;
        try {
            c = Class.forName(cnm);
            ret = c.newInstance();

        } catch (Exception e) {
            E.error("cant instantiate " + cnm + " " + e);
        }
        return ret;
    }



    public Object newInstance(String cnm, Object arg) {
        Object ret = null;
        Class<?> c = null;
        try {
            c = Class.forName(cnm);
            Class[] ca = {arg.getClass()};
            Constructor con = c.getConstructor(ca);
            Object[] oa = {arg};
            ret = con.newInstance(oa);

        } catch (Exception e) {
            E.error("cant instantiate " + cnm + " " + e);
        }
        return ret;
    }



    public Class<?> forName(String s) {
        return forName(s, false);
    }


    public Class<?> forName(String scl, boolean require) {
        Class<?> ret = null;
        try {
            ret = Class.forName(scl);
        } catch (Exception ex) {
            if (require) {
                E.error("cant instantiate: " + scl + " " + ex);
            }
        }
        return ret;
    }

    public void reset() {
        E.missing("reset does nothing on the class instantiator");
    }

    public ClassLoader getLoader() {
        return null;
    }

}
