package org.catacomb.interlish.reflect;

import org.catacomb.be.Instantiator;
import org.catacomb.interlish.content.BooleanValue;
import org.catacomb.interlish.content.DoubleValue;
import org.catacomb.interlish.content.IntegerValue;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.resource.ImportContext;
import org.catacomb.interlish.resource.ResourceRole;
import org.catacomb.interlish.resource.Role;
import org.catacomb.interlish.service.ContentLoader;
import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.StringTokenizer;

import java.lang.reflect.Array;



public class ReflectionConstructor implements Constructor {

    ArrayList search = new ArrayList();

    int npkg;
    String[] pkgs;

    String wkpkg;

    ImportContext importContext;

    Instantiator instantiator;


    static ArrayList<String> paths;

    static {
        paths = new ArrayList<String>();
    }

    public static void addPath(String s) {
        paths.add(s);
    }



    public ReflectionConstructor() {
        pkgs = new String[100];
        npkg = 0;
        instantiator = new ClassInstantiator();
        for (String s : paths) {
            addSearchPackage(s);
        }
    }


    public ReflectionConstructor(String path) {
        this();
        addSearchPackage(path);
    }


    public void setInstantiator(Instantiator inst) {
        instantiator = inst;
    }


    public void setImportContext(ImportContext ctx) {
        importContext = ctx;
    }



    public void addSearchPackage(String s) {
        wkpkg = s;
        pkgs[npkg++] = s;
    }



    public void appendContent(Object obj, String s) {
        E.error(" - reflection instantiator doesn't do appendContent on " + obj);
    }


    public void checkAddPackage(Object oret) {
        String scl = oret.getClass().getName();
        if (scl.startsWith("java")) {
            return;
        }

        int ild = scl.lastIndexOf(".");
        String pkg = scl.substring(0, ild);
        if (pkg.equals(wkpkg)) {
            // just same as before;

        } else {
            boolean got = false;
            for (int i = 0; i < npkg; i++) {
                if (pkgs[i].equals(pkg)) {
                    got = true;
                    break;
                }
            }
            if (!got) {
                pkgs[npkg++] = pkg;

                // System.out.println("Reflection instantiator added search package
                // " + pkg);
            }
        }
    }



    public Object newInstance(String scl) {
        Object oret = null;
        Class<?> c = null;
        boolean fcnm = scl.startsWith("org.");
        if (scl.indexOf(".") > 0) {
            c = instantiator.forName(scl, fcnm);
        }

        if (c == null && !fcnm) {
            for (int i = 0; i < npkg && c == null; i++) {
                c = instantiator.forName(pkgs[i] + "." + scl);
            }
        }

        if (c == null) {
            E.error("cannot instantiate (class not found): " + scl + "\n" +
                    " instantiator: " + instantiator);
            E.reportCached();

            if (npkg == 0) {
                E.info("There are no search packages configured!");
            }
            for (int i = 0; i < npkg; i++) {
                E.info("tried package " + pkgs[i]);
            }

            if (scl.endsWith("ing")) {
                (new Exception()).printStackTrace();
            }


        } else {
            int imod = c.getModifiers();
            if (Modifier.isAbstract(imod)) {
                E.error("cannot instantiatie " + c + ":  it is an abstract class");
            } else {

                try {
                    oret = c.newInstance();
                } catch (Exception e) {
                    E.error(" " + e + " instantiating " + c);
                    e.printStackTrace();
                }
            }
        }

        if (oret != null) {
            checkAddPackage(oret);
        }

        return oret;
    }



    public Object getField(Object ob, String fnm) {
        Object ret = null;

        boolean hasField = false;

        // EFF improve
        Field[] flds = ob.getClass().getFields();
        for (int i = 0; i < flds.length; i++) {
            if (flds[i].getName().equals(fnm)) {
                hasField = true;
                break;
            }
        }

        if (hasField) {
            try {
                Field f = ob.getClass().getField(fnm);

                Class fcl = f.getType();

                if (fcl.equals(String[].class)) {
                    ret = new String[0];

                } else if (fcl.isArray()) {
                    ret = new ArrayList(); // ADHOC - wrap ArrayList?
                } else {
                    ret = f.get(ob);
                }

                if (ret == null) {
                    Class<?> cl = f.getType();
                    ret = cl.newInstance();
                }

            } catch (Exception e) {
                E.error("cannot get field " + fnm + " on " + ob + " " + "excception= " + e);
            }
        }


        /*
         * if (!hasField && ob instanceof FieldValueProvider) { ret =
         * ((FieldValueProvider)ob).getFieldValue(fnm); if (ret != null) {
         * hasField = true; } }
         */


        if (!hasField) {
            if (ob instanceof ArrayList) {
                // we're OK - the object will just be added;

            } else {

                // System.out.println("error - cannot get field " + fnm + " on " +
                // ob);
                /*
                 * Field[] af = ob.getClass().getFields(); for (int i = 0; i <
                 * af.length; i++) { System.out.println("fld " + i + " " + af[i]); }
                 */
            }
        }
        return ret;
    }



    public Object getChildObject(Object parent, String name, Attribute[] attain) {
        Attribute[] atta = attain;
        Object child = null;

        if (parent != null) {
            checkAddPackage(parent); // EFF inefficient
        }

        // Three possibilities:
        // 1 there is an attribute called class;
        // 2 the parent has a field called name;
        // 3 the name is a class name;


        if (atta == null) {
            atta = new Attribute[0];
        }


        // process special attributes and instantiate child if class is known
        // (case 1);
        String classname = null;
        for (int i = 0; i < atta.length; i++) {
            Attribute att = atta[i];
            String attName = att.getName();
            String attValue = att.getValue();

            if (attName.equals("package")) {
                StringTokenizer stok = new StringTokenizer(attValue, ", ");
                while (stok.hasMoreTokens()) {
                    addSearchPackage(stok.nextToken());
                }

            } else if (attName.equals("archive-hash")) {
                if (importContext != null) {
                    child = importContext.getRelative(attValue);

                } else {
                    E.debugError("xmlreader found reference to archive file "
                                 + " but has no importContext to retrieve object");
                }

            } else if (attName.equals("class")) {
                classname = attValue;
            }
        }


        // not in the above loop because want to parse packages first;
        if (child == null && classname != null) {
            child = newInstance(classname);
        }


        // dont know the class - the parent may know it; (CASE 2)
        if (child == null && parent != null) { // / && hasField(parent, name)) {
            child = getField(parent, name);
        }


        if (child == null && name.equals("Role")) {
            child = new ResourceRole();
        }

        // or perhaps the open tag is a class name? (CASE 3)
        if (child == null) {

            // ADHOC - dont really want this dependence - pull out providers for
            // reflector?

            if (ResourceAccess.hasContentLoader()) {
                ContentLoader cl = ResourceAccess.getContentLoader();
                if (cl.hasProviderOf(name)) {
                    child = cl.getProviderOf(name);
                    checkAddPackage(child);
                }
            }
            if (child == null) {
                child = newInstance(name);
            }
        }


        if (child == null) {
            E.warning("ReflectionInstantiator failed to get field " + name + " on " + parent + " "
                      + (parent != null ? parent.getClass().toString() : ""));
        }


        /*
         * POSERR did this do anything useful? if (child instanceof IDd &&
         * ((IDd)child).getID() == null) { // setAttributeField(child, "id",
         * name); // System.out.println("autoset id to " + name); }
         */
        return child;
    }



    public void applyAttributes(Object target, Attribute[] atta) {

        for (int i = 0; i < atta.length; i++) {
            Attribute att = atta[i];
            setAttributeField(target, att.getName(), att.getValue());
        }
    }



    public boolean setAttributeField(Object target, String name, String arg) {
        boolean bret = false;
        if (name.equals("class") || name.equals("package") || name.equals("provides")
                || name.equals("archive-hash")) {
            // already done; ADHOC

        } else {
            bret = setField(target, name, arg);
        }

        return bret;
    }


    // ADHOC suppressing warnings
    @SuppressWarnings( { "unchecked" })
    public boolean setField(Object ob, String sfin, Object argin) {
        String sf = sfin;
        Object arg = argin;
        if (arg instanceof StringValue) {
            arg = ((StringValue)arg).getString();

        } else if (arg instanceof BooleanValue) {
            arg = new Boolean(((BooleanValue)arg).getBoolean());

        } else if (arg instanceof IntegerValue) {
            arg = new Integer(((IntegerValue)arg).getInteger());

        } else if (arg instanceof DoubleValue) {
            arg = new Double(((DoubleValue)arg).getDouble());
        }



        // System.out.println("setting field " + sf + " in " + ob + " to " + arg);
        if (ob == null) {
            E.error("null parent for " + sf + " (" + arg + ")");
            return true;
        }

        if (arg == null) {
            // dont think this should be an error? - fine to set something to null on instantiation?
            // E.error("reflection instantiator has null arg setting " + sf + " in " + ob);
            return true;
        }

        if (arg.equals(ob)) {
            E.error("ReflectionInstantiator setField: " + "the child is the same as the parent " + ob);
            return true;
        }

        int icolon = sf.indexOf(":");
        if (icolon >= 0) {
            sf = sf.substring(0, icolon) + "_" + sf.substring(icolon + 1, sf.length());
        }

        boolean ok = false;

        Class c = ob.getClass();
        Field f = null;
        try {
            f = c.getField(sf);
        } catch (NoSuchFieldException e) {
        }



        if (f == null) {
            if (ob instanceof ArrayList) {
                ((ArrayList)ob).add(arg);
                ok = true;

            } else if (arg instanceof String && ob instanceof AttributeAddableTo) {
                ((AttributeAddableTo)ob).addAttribute(sf, (String)arg);
                ok = true;

            } else if (ob instanceof AddableTo && nonPrimitive(arg)) {
                ((AddableTo)ob).add(arg);
                ok = true;


            } else if (arg instanceof Role) {
                // just ignore these silently....;
                ok = true;

            } else {
                E.linkToWarning("no such field " + sf, ob);
                E.reportCached();
                ok = false;
            }

        } else {
            // POSERR avoids warning but bit silly
            if (arg instanceof ArrayList && ((ArrayList)arg).size() == 1) {
                if (f.getType().isArray()) {
                    // fine as it is,

                } else {
                    // arg is in a list for packing convenience - get it out
                    for (Object sub : (ArrayList)arg) {
                        arg = sub;
                    }
                }
            }

            try {
                Class ftyp = f.getType();
                if (ftyp == String.class && arg instanceof String) {
                    f.set(ob, arg);

                } else if (ftyp == Double.TYPE && arg instanceof String) {
                    Double d = new Double((String)arg);
                    f.set(ob, d);

                } else if (ftyp == Double.TYPE && arg instanceof Double) {
                    f.set(ob, arg);

                } else if (ftyp == Boolean.TYPE && arg instanceof Boolean) {
                    f.set(ob, arg);

                } else if (ftyp == Integer.TYPE && arg instanceof Integer) {
                    f.set(ob, arg);

                } else if (f.getType().isArray() && arg instanceof ArrayList) {
                    setArrayField(ob, f, (ArrayList)arg);

                } else {
                    Object onarg = Narrower.narrow(ftyp.getName(), arg);

                    if (onarg != null) {
                        f.set(ob, onarg);
                    } else {
                        f.set(ob, arg);
                    }
                }
                ok = true;
            } catch (Exception e) {
                ok = false;
                E.error(" cannot set field " + sf + " in " + ob + " from typed " +
                        arg.getClass().getName() + " value " + arg + " " + e);
            }
        }


        return ok;
    }



    public void setArrayField(Object obj, Field fld, ArrayList vals) {
        /*
         * E.missing(); System.out.println("setting array field of " + vals.size() + "
         * in " + obj + " fnm=" + fld.getName());
         */

        try {
            int nv = vals.size();

            Class acls = fld.getType();
            Class ccls = acls.getComponentType();

            Object avals = Array.newInstance(ccls, nv);
            for (int i = 0; i < nv; i++) {
                Array.set(avals, i, vals.get(i));
            }
            fld.set(obj, avals);
        } catch (Exception ex) {
            E.error(" - cannot setaray field " + fld + " " + vals);
        }
    }




    private boolean nonPrimitive(Object arg) {
        boolean ret = true;
        if (arg instanceof String || arg instanceof Integer || arg instanceof Double) {
            ret = false;
        }
        return ret;
    }


    public void setIntFromStatic(Object ret, String id, String sv) {
        String svu = sv.toUpperCase();
        Object obj = getField(ret, svu);
        if (obj instanceof Integer) {
            setField(ret, id, obj);
        } else {
            E.error("need an Integer, not  " + obj);
        }
    }

}
