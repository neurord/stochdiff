package org.catacomb.druid.gui.base;


import org.catacomb.interlish.structure.ActionRelay;
import org.catacomb.report.E;


import java.lang.reflect.Method;
import java.util.Hashtable;


public class DruActionRelay implements ActionRelay {


    Object actor;

    Hashtable<String, Method> methodHT;


    public DruActionRelay(Object ao) {
        actor = ao;


        methodHT = new Hashtable<String, Method>();
        try {
            Method[] methods = actor.getClass().getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                methodHT.put(m.getName(), m);
            }


        } catch (Exception ex) {

            E.error(" making action connector " + ex);
        }
    }


    public Object getActor() {
        return actor;
    }



    public void action(String methodName) {
        invokeMethod(methodName, new Object[0]);
    }


    public void actionB(String methodName, boolean b) {
        Boolean[] args = { new Boolean(b) };
        invokeMethod(methodName, args);
    }

    public void actionI(String methodName, int i) {
        Integer[] args = { new Integer(i) };
        invokeMethod(methodName, args);
    }

    public void actionD(String methodName, double d) {
        Double[] args = { new Double(d) };
        invokeMethod(methodName, args);
    }



    public void actionO(String methodName, Object obj) {
        Object[] args = {obj};
        invokeMethod(methodName, args);
    }


    private Boolean getBoolean(String sarg) {
        Boolean ret = null;
        if (sarg != null) {
            if (sarg.equals("true") || sarg.equals("on")) {
                ret = new Boolean(true);

            } else if (sarg.equals("false") || sarg.equals("off")) {
                ret = new Boolean(false);
            }

        }
        return ret;
    }



    public void actionS(String methodName, String sarg) {

        Boolean bl = getBoolean(sarg);

        if (bl == null)
            if (sarg == null) {
                invokeMethod(methodName, null);
            } else {
                String[] args = { sarg };
                invokeMethod(methodName, args);
            }

        else {
            Boolean[] args = { bl };
            invokeMethod(methodName, args);

        }
    }



    public void invokeMethod(String methodName, Object[] args) {


        if (methodName != null && methodName.length() > 0) {

            Class[] ca;

            if (args == null) {
                ca = new Class[0];
            } else {
                ca = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    Object oa = args[i];
                    if (oa == null) {

                    } else if (oa instanceof Boolean) {
                        ca[i] = boolean.class;

                    } else if (oa instanceof Double) {
                        ca[i] = double.class;

                    } else if (oa instanceof Integer) {
                        ca[i] = int.class;

                    } else {
                        ca[i] = args[i].getClass();
                    }
                }
            }


            try {

                // EFF - used to hash these ?;
                Method meth = actor.getClass().getDeclaredMethod(methodName, ca);
                meth.invoke(actor, args);

            } catch (NoSuchMethodException ex) {
                String sarg = "";
                int narg = 0;
                if (ca != null && ca.length > 0) {
                    narg = ca.length;
                    sarg += "(";
                    for (int i = 0; i < ca.length; i++) {
                        if (i > 0) {
                            sarg += ",";
                        }
                        sarg += ca[i];
                    }
                    sarg += ")";
                } else {
                    sarg = "()";
                }
                E.linkToWarning("AC no method " + methodName + " on " + actor +
                                " with arg list (n=" + narg + ") " + sarg, actor);
                E.warning("call sequence: ");

            } catch (Exception ex) {
                E.error("AC exception while invoking " + methodName +
                        " on " + actor + " ex=" + ex);
                E.info("args were " + args);
                ex.printStackTrace();
            }

        } else {
            E.warning("No method set in action relay");
            // E.printStackTrace();
        }
    }




}
