package org.catacomb.interlish.reflect;

import org.catacomb.interlish.structure.Accessor;
import org.catacomb.interlish.structure.Structure;
import org.catacomb.interlish.structure.Targeted;
import org.catacomb.report.E;


import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class ObjectAccessor implements Structure, Targeted, Accessor {

    Object target;
    String typeName;


    static Class[] emptyCA;
    static Object[] emptyOA;

    static {
        emptyCA = new Class[0];
        emptyOA = new Object[0];
    }


    public ObjectAccessor(Object tgt) {
        target = tgt;
        typeName = target.getClass().getName();
    }

    public Object getTarget() {
        E.deprecate("use Accessor, not Targeted");
        return target;
    }

    public Object getAccessee() {
        return target;
    }

    public String getTypeName() {
        return typeName;
    }


    public Object get(String s) {
        Object ret = null;
        try {
            ret = getByGetter(s);

        } catch (NoSuchMethodException ex) {
            try {
                ret = getDirectly(s);
            } catch (NoSuchFieldException ex2) {
                E.error("no field called " + s + " in " + target);
            }
        }
        if (ret == null && s.equals("name")) {
            E.warning("null name on " + target.getClass().getName());
        }
        return ret;
    }


    public Object getDirectly(String s) throws NoSuchFieldException {
        Object ret = null;

        Field fld = target.getClass().getField(s);
        try {
            ret = fld.get(target);
        } catch (Exception ex) {
            E.error("object has field but cannot access " + s + " " + target);
        }
        return ret;
    }



    public Object getByGetter(String s) throws NoSuchMethodException {
        Object ret = null;

        String mnm = "get" + s;

        Method m = target.getClass().getMethod(mnm, emptyCA);
        try {
            ret = m.invoke(target, emptyOA);
        } catch (Exception ex) {
            E.error("get method exists but cannot use it: " + ex);
        }
        return ret;
    }



    public Object getStatic(String s) {
        E.missing();
        return null;
    }

}
