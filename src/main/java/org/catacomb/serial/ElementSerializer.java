package org.catacomb.serial;

import org.catacomb.interlish.structure.Attribute;
import org.catacomb.interlish.structure.Element;
import org.catacomb.report.E;
import org.catacomb.serial.xml.XMLElementWriter;


import java.util.Collection;

public class ElementSerializer {



    public static String serializeContent(Element elt) {

        StringBuffer sb = new StringBuffer();
        String psk = "";

        Collection<Element> elts = elt.getElements();
        if (elts != null) {
            for (Element child : elts) {
                XMLElementWriter.appendElement(sb, psk, child);
            }
        }

        Collection<Attribute> atts = elt.getAttributes();
        if (atts != null && atts.size() > 0) {
            E.warning("ignoring attributes in element content serialization " + atts);
        }

        String btxt = elt.getText();
        sb.append(btxt);

        return sb.toString();
    }



}


