package org.catacomb.interlish.reflect;

import org.catacomb.interlish.structure.SettableStructure;
import org.catacomb.report.E;


import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class ReflectionWrapper implements SettableStructure {


    Object target;

    String fullClassname;

    // HashMap methodHM;

    Class<?> m_cls;

    static Class[] emptyCA;
    static Object[] emptyOA;


    static {
        emptyCA = new Class[0];
        emptyOA = new Object[0];
    }



    public ReflectionWrapper(String pkg, String cnm) {
        String fcnm = cnm;
        if (pkg != null && pkg.length() > 0) {
            fcnm = pkg + "." + cnm;
        }

        fullClassname = fcnm;

        try {
            m_cls = Class.forName(fcnm);

            target = m_cls.newInstance();

            /*
             * methodHM = new HashMap();
             *
             * Method[] ma = cls.getDeclaredMethods(); for (int i = 0; i <
             * ma.length; i++) { methodHM.put(ma[i].getName(), ma[i]); }
             */


        } catch (Exception ex) {
            E.error(" - can't construct " + fcnm + " " + ex);
        }

    }


    public boolean isOK() {
        return (target != null);
    }


    public Object getTarget() {
        return target;
    }


    public String getTypeName() {
        return fullClassname;
    }


    public Object get(String s) {
        return getByGetter(s);
    }


    public Object getStatic(String s) {
        return getDirectly(s);
    }


    public Object getDirectly(String s) {
        Object ret = null;
        try {
            Field fld = m_cls.getField(s);
            ret = fld.get(target);
        } catch (Exception ex) {
            System.out.println("WARNING - cannot get (direct access) field " + s + " on " + target);
        }
        return ret;
    }



    public Object getByGetter(String s) {
        Object ret = null;

        String mnm = "get" + s;

        try {
            Method m = m_cls.getMethod(mnm, emptyCA);
            ret = m.invoke(target, emptyOA);

        } catch (Exception ex) {
            System.out.println("WARNING - cannot get (getter method) field " + s + " on " + target);
        }

        return ret;
    }



    public void set(String s, Object val) {
        setBySetter(s, val);
    }

    public void setContext(String s, Object val) {
        E.missing();
    }

    public void setBySetter(String s, Object val) {
        if (val == null) {
            return;
        }

        String fl = s.substring(0, 1);
        String mnm = "set" + fl.toUpperCase() + s.substring(1, s.length());

        Class argcls = null;
        if (val instanceof Double) {
            argcls = Double.TYPE;
        } else if (val instanceof Integer) {
            argcls = Integer.TYPE;
        } else if (val instanceof Boolean) {
            argcls = Integer.TYPE;
        } else {
            argcls = val.getClass();
        }


        Class[] ca = { argcls };
        Object[] args = { val };


        try {
            Method m = m_cls.getMethod(mnm, ca);
            m.invoke(target, args);

        } catch (Exception ex) {
            System.out.println("WARNING - cannot set field " + s + " on " + target + " by executing "
                               + mnm + " with arg " + val + " " + val.getClass().getName() + " " + ex);
        }
    }


}
