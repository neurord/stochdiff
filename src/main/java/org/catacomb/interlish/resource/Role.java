package org.catacomb.interlish.resource;

import org.catacomb.interlish.structure.Attribute;
import org.catacomb.interlish.structure.Element;
import org.catacomb.interlish.structure.ElementReader;
import org.catacomb.report.E;




public abstract class Role implements ElementReader {

    public String resource;
    public String function;
    public String value;


    private Object p_cached;   // POSERR not sure want this?


    public Role() {

    }



    public Role(String sr, String sf, String sv) {
        resource = sr;
        function = sf;
        value = sv;
    }


    public void populateFrom(Element elt) {

        for (Attribute att : elt.getAttributes()) {
            String sn = att.getName();
            String sv = att.getValue();
            if (sn.equals("resource")) {
                resource = sv;
            } else if (sn.equals("function")) {
                function = sv;
            } else if (sn.equals("value")) {
                value = sv;

                // alt form;
            } else if (function == null) {
                function = sn;
                value = sv;
            } else {
                E.error("role reading alreadyu have function (" + function + ") but read " +
                        sn + " " + sv);
            }
        }
    }


    public void setResource(String s) {
        resource = s;
    }

    public String getResource() {
        return resource;
    }


    public String getFunction() {
        return function;
    }


    public String getValue() {
        return value;
    }


    public void cachePlayer(Object obj) {
        p_cached = obj;
    }

    public boolean hasCachedPlayer() {
        return (p_cached != null);
    }

    public Object getCachedPlayer() {
        return p_cached;
    }


}
