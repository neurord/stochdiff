package org.catacomb.serial;

import org.catacomb.interlish.structure.Element;
import org.catacomb.serial.xml.XMLElementWriter;



public class Serializer {



    public static String serialize(Object obj) {
        if (obj instanceof String) {
            return (String)obj;
        }

        OmElementizer elementizer = new OmElementizer(new SerializationContext());



        Element elt = null;

        if (obj instanceof Element) {
            elt = (Element)obj;
        } else {
            elt = elementizer.getElement(obj);
        }

        StringBuffer sb = new StringBuffer();
        String psk = "";
        XMLElementWriter.appendElement(sb, psk, elt);

        return sb.toString();
    }



}
