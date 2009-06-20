package org.catacomb.serial;

import org.catacomb.interlish.structure.Element;
import org.catacomb.serial.om.ElementConstructor;
import org.catacomb.serial.xml.XMLReader;
import org.catacomb.util.FileUtil;


import java.io.File;


public class ElementImporter {

    XMLReader xmlReader;





    public static Element getElement(File f) {
        ElementImporter eim = new ElementImporter();
        return eim.readElement(f);
    }


    public ElementImporter() {
        ElementConstructor ein = new ElementConstructor();
        xmlReader = new XMLReader(ein);
    }



    public Element readElement(File f) {
        String s = FileUtil.readStringFromFile(f);
        Object eltobj = xmlReader.readObject(s);
        Element elt = (Element)eltobj;
        return elt;
    }



}
