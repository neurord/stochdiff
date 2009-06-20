
package org.catacomb.serial.om;

import org.catacomb.interlish.structure.Attribute;
import org.catacomb.interlish.structure.Constructor;
import org.catacomb.report.E;




public class ElementConstructor implements Constructor {


    public Object newInstance(String s) {
        return new OmElement(s);
    }


    public boolean setAttributeField(Object parent, String fieldName, String val) {
        if (parent instanceof OmElement) {
            OmElement omp = (OmElement)parent;
            omp.addAttribute(fieldName, val);
        }
        return true;
    }


    public void appendContent(Object obj, String s) {
        ((OmElement)obj).addToBody(s);
    }


    public Object getChildObject(Object parent, String name, Attribute[] atta) {

        OmElement elt = new OmElement(name);

        return elt;
    }


    public void applyAttributes(Object obj, Attribute[] atta) {
        ((OmElement)obj).copyAttributes(atta);
    }



    public boolean setField(Object parent, String fieldName, Object child) {
        boolean ok = false;

        if (parent instanceof OmElement && child instanceof String) {

            OmElement omp = (OmElement)parent;
            OmElement ec = new OmElement(fieldName);

            ec.setBody((String)child);
            omp.addElement(ec);


        } else if (parent instanceof OmElement && child instanceof OmElement) {
            OmElement omp = (OmElement)parent;
            OmElement omc = (OmElement)child;

            if (omc.getName().equals(fieldName)) {
                omp.addElement(omc);
                ok = true;

            } else {
                E.error(" - element instantiator set field hs fieldname " +
                        fieldName + "  but element " + omc.getName());
            }

        } else {
            E.error(" - ElementInstantiator set field : fieldname=" + fieldName +
                    " parent=" + parent +
                    "    child=" + child + " " + child.getClass().getName() +
                    "  but need elements only");
            (new Exception()).printStackTrace();
        }
        return ok;
    }


    public Object getField(Object parent, String fieldName) {
        return new OmElement(fieldName);
    }


    public void setIntFromStatic(Object ret, String id, String sv) {
        E.missing();
    }


}
