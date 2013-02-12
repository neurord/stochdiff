
package org.catacomb.util;


import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


import java.awt.Color;
import java.util.HashMap;


public class StandardColors implements AddableTo {

    HashMap<String, Color> colors;

    public StandardColors() {
        colors = new HashMap<String, Color>();
    }


    public boolean defines(String s) {
        return colors.containsKey(s);
    }

    public Color getColor(String s) {
        return colors.get(s);
    }



    public void add(Object obj) {
        if (obj instanceof ColorDef) {
            ColorDef cdef = (ColorDef)obj;
            colors.put(cdef.getName(), cdef.getColor());

        } else {
            E.error("standard colors cannot use " + obj);
        }
    }


}
