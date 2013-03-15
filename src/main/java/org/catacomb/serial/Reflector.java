package org.catacomb.serial;

import org.catacomb.be.DeReferencable;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;
import org.catacomb.serial.om.OmAttribute;
import org.catacomb.serial.om.OmElement;


import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;



public class Reflector {



    public static Element makeObjectElementByReflection(Object obj,
            OmElementizer elementizer) {


        SerializationContext ctxt = elementizer.getContext();


        if (obj instanceof DeReferencable) {
            ((DeReferencable)obj).deReference();
        }

        // REFAC call methods from eltfac;

        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        ArrayList<Element> elements = new ArrayList<Element>();


        String scnm = obj.getClass().getName();
        int ild = scnm.lastIndexOf(".");
        String spk = scnm.substring(0, ild);
        String ss = scnm.substring(ild + 1, scnm.length());


        String eltname = ss;

        if (elementizer.getContext().shouldWritePackage(spk)) {
            attributes.add(new OmAttribute("package", spk));
        }



        ArrayList<Field> af = new ArrayList<Field>();
        Class<?> cls = obj.getClass();
        do {
            Field[] declFields = cls.getDeclaredFields();
            for (Field fld : declFields) {
                af.add(fld);
            }
            AccessibleObject.setAccessible(declFields, true);
            cls = cls.getSuperclass();
        } while (cls != null);


        for (Field field : af) {
            String name = field.getName();
            int modif = field.getModifiers();

            if (name.startsWith("p_") || name.startsWith("r_")) {
                // ignore these (!!!)

            } else if (name.startsWith("s_")) {
                Object val = getFieldValue(field, obj);
                if (val == null) {
                    // just leave it out entirely ? POSERR
                    E.warning("found special field " + name + " but value is null");

                } else {
                    int hcode = val.hashCode();
                    if (ctxt.acceptsReferents()) {

                        ctxt.addReferent("" + hcode, val);

                        OmElement elt = new OmElement();
                        elt.setName(name);
                        elt.addAttribute("archive-hash", "" + hcode);
                        elements.add(elt);

                    } else {
                        E.warning(" - (Reflector) special object " + "not saved " + name + " " + val);
                    }
                }

            } else if (Modifier.isFinal(modif) && Modifier.isStatic(modif)) {
                // don't save these either;

            } else {
                Object val = getFieldValue(field, obj);



                if (val == null) {
                    // just ignore these

                } else if (val instanceof Map) {
                    E.error("raw map in reflector as child of " + obj);
                    E.error(" - ignoring " + val);

                } else if (val instanceof Stateless) {
                    // ignore these too;

                } else if (val instanceof String) {
                    attributes.add(new OmAttribute(name, (String)val));

                } else {
                    if (ctxt.recurseAll() || val instanceof int[][] || val instanceof double[][]) {

                        Element elt = elementizer.makeElement(val);
                        OmElement omelt = (OmElement)elt;
                        if (omelt != null) {
                            omelt.setName(name);
                            elements.add(omelt);
                        }

                    }
                }
            }
        }
        OmElement elt = new OmElement(attributes, elements);
        elt.setName(eltname);
        return elt;
    }



    public static Object getFieldValue(Field field, Object obj) {

        Object val = null;
        try {
            val = field.get(obj);

        } catch (Exception ex) {
            E.error(" getting field " + field.getName() + " on " + obj.getClass().getName() + " " + ex);
            val = "ERROR";
        }


        return SerialUtil.stringifyObject(val);
    }



}
