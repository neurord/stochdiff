package org.catacomb.serial;

import org.catacomb.interlish.structure.Element;
import org.catacomb.interlish.structure.ElementWriter;
import org.catacomb.interlish.structure.Elementizer;
import org.catacomb.report.E;
import org.catacomb.serial.om.OmElement;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class OmElementizer implements Elementizer {

    SerializationContext ctxt;


    public OmElementizer() {
        ctxt = new SerializationContext();
    }


    public OmElementizer(SerializationContext sc) {
        ctxt = sc;
    }

    public SerializationContext getContext() {
        return ctxt;
    }

    public Element getElement(Object obj) {
        return makeElement(obj);
    }


    public Element getFlatElement(Object obj) {
        ctxt.setPrimitivesOnly();
        return makeElement(obj);
    }


    public Element makeElement(Object objin) {
        Object obj = objin;

        Element retelt = null;

        if (obj instanceof char[]) {
            obj = new String((char[])obj);
        }

        if (obj instanceof String) {
            retelt = makeStringElement((String)obj);

        } else if (obj instanceof int[]) {
            retelt = makeIntArrayElement((int[])obj);

        } else if (obj instanceof double[]) {
            retelt = makeDoubleArrayElement((double[])obj);

        } else if (obj instanceof List) {
            retelt = makeListElement((List)obj);

        } else if (obj != null && obj.getClass().isArray()) {
            retelt = makeArrayElement(obj);

        } else if (obj instanceof Map) {
            E.error("Elementizer asked to elementize a raw map - shouldn't happen");

        } else if (obj != null) {
            retelt = makeObjectElement(obj);
        }
        return retelt;
    }



    private Element makeStringElement(String s) {
        OmElement elt = new OmElement("String");
        elt.addAttribute("value", s);
        return elt;
    }


    private Element makeIntArrayElement(int[] ia) {
        OmElement elt = new OmElement("IntegerArray");
        elt.addAttribute("values", SerialUtil.stringify(ia));
        return elt;
    }


    private Element makeDoubleArrayElement(double[] da) {
        OmElement elt = new OmElement("DoubleArray");
        elt.addAttribute("values", SerialUtil.stringify(da));
        return elt;
    }



    private  Element makeListElement(List list) {
        OmElement listelt = new OmElement();

        for (Object listobj : list) {

            Element elt = makeElement(listobj);

            listelt.addElement(elt);
        }
        return listelt;
    }




    private  Element makeArrayElement(Object arr) {
        ArrayList<Object> arl = new ArrayList<Object>();
        try {
            int nel = Array.getLength(arr);
            for (int i = 0; i < nel; i++) {
                Object listobj = Array.get(arr, i);
                if (listobj != null) {
                    arl.add(listobj);
                }
            }
        } catch (Exception ex) {
            System.out.println("ERROR - sand Reflector cannot handle " + arr + " " + ex);
        }
        return makeListElement(arl);
    }



    private Element makeObjectElement(Object obj) {
        Element ret = null;
        if (obj instanceof ElementWriter) {
            ret = ((ElementWriter)obj).makeElement(ctxt.getElementFactory(), this);

        } else {
            ret = Reflector.makeObjectElementByReflection(obj, this);
        }
        return ret;

    }

    public Element elementize(Object obj) {
        return makeObjectElement(obj);
    }

}
