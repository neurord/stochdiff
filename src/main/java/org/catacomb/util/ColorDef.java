package org.catacomb.util;


import org.catacomb.report.E;

import java.awt.Color;


public class ColorDef {

    public String name;

    public String value;

    Color cvalue;




    public String getName() {
        return name;
    }


    public Color getColor() {
        if (cvalue == null) {
            try {
                int ic = Integer.decode(value).intValue();
                cvalue = new Color(ic);

            } catch (NumberFormatException ex) {
                E.error(" - ColorDef cannot decode color string " + value);
                cvalue = Color.red;
            }
        }
        return cvalue;
    }

}
