package org.catacomb.serial.xml;

import org.catacomb.interlish.structure.Attribute;
import org.catacomb.interlish.structure.Element;

import java.util.Collection;



public class XMLElementWriter {



    public static void appendElement(StringBuffer sbv,
                                     String psk,
                                     Element elt) {

        Collection<Attribute> atts = elt.getAttributes();
        Collection<Element> elts = elt.getElements();
        String stxt = elt.getText();

        boolean hasElts = false;
        if (elts != null && elts.size() > 0) {
            hasElts = true;
        }

        boolean hasBody = false;
        if (stxt != null && stxt.length() > 0) {
            hasBody = true;
        }


        sbv.append(psk);
        sbv.append("<");
        sbv.append(elt.getName());

        if (atts != null) {
            boolean first = true;
            for (Attribute att : atts) {
                if (first) {
                    sbv.append(" ");
                } else {
                    sbv.append("\n");
                    sbv.append(psk);
                    sbv.append("    ");
                }
                sbv.append(att.getName());
                sbv.append("=\"");
                sbv.append(att.getValue());
                sbv.append("\"");
                first = false;
            }
        }


        if (hasElts || hasBody) {
            sbv.append(">");

            if (hasElts) {
                sbv.append("\n");
                for (Element subelt : elts) {
                    appendElement(sbv, psk + "   ", subelt);
                }
                sbv.append(psk);
            }

            if (hasBody) {
                sbv.append(stxt);
            }
            sbv.append("</");
            sbv.append(elt.getName());
            sbv.append(">\n");

        } else {
            sbv.append("/>\n");
        }
    }
}
