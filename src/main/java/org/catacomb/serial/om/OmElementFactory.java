package org.catacomb.serial.om;

import org.catacomb.interlish.structure.Element;
import org.catacomb.interlish.structure.ElementFactory;
import org.catacomb.interlish.structure.Specified;
import org.catacomb.interlish.structure.Specifier;


public class OmElementFactory implements ElementFactory {



    public OmElementFactory() {

    }

    // for elements that should ocntain all their context as attibutes (ie, the package)
    public Element makeStandaloneElementFor(Object obj) {
        Element elt = null;

        if (obj instanceof Specified) {
            elt = makeSpecifiedElement((Specified)obj);

        } else {
            elt = makeClassElement(obj);
        }

        return elt;
    }



    private Element makeSpecifiedElement(Specified spd) {
        Specifier sp = spd.getSpecifier();

        String sch = sp.getSpecifiedTypeName(spd);

        OmElement ome = new OmElement();
        ome.setName(sch);
        return ome;
    }


    private Element makeClassElement(Object obj) {
        String cnm = obj.getClass().getName();
        int ild = cnm.lastIndexOf(".");
        String pkg = "";
        String enm = cnm;
        if (ild >= 0) {
            enm = cnm.substring(ild+1, cnm.length());
            pkg = cnm.substring(0, ild);
        }
        OmElement elt = new OmElement();
        elt.setName(enm);
        elt.addAttribute("package", pkg);
        return elt;
    }



    // for elements within a known context - just get the class name and use it for the name
    public Element makeElementFor(Object obj) {
        String  cnm = obj.getClass().getName();
        int ild = cnm.lastIndexOf(".");
        if (ild >= 0) {
            cnm = cnm.substring(ild+1, cnm.length());
        }
        OmElement elt = new OmElement();
        elt.setName(cnm);
        return elt;
    }



    public Element makeElement(String name) {
        OmElement elt = new OmElement();
        elt.setName(name);
        return elt;
    }

    public Element makeElement(String name, String body) {
        OmElement elt = new OmElement();
        elt.setName(name);
        elt.setBody(body);
        return elt;
    }





    // in following the objects are whatever class is returned by the above two;
    public void addAttribute(Element elt, String name, String value) {
        ((OmElement)elt).addAttribute(name, value);
    }


    public void addAttribute(Element elt, String name, double value) {
        ((OmElement)elt).addAttribute(name, "" + value);
    }


    public void addElement(Element parent, Object child) {
        ((OmElement)parent).addElement((OmElement)child);
    }


}

