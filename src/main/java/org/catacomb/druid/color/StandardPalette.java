package org.catacomb.druid.color;


import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.report.E;
import org.catacomb.util.StandardColors;

import java.awt.Color;

public class StandardPalette {

    static Color[] colors;

    static String[] colorNames= {"BLACK", "MIDBLUE", "MIDGREEN",
                                 "MIDRED",
                                 "BROWN", "PINK", "ORANGE", "MAGENTA"
                                };



    public static Color getColor(int icol) {
        if (colors == null) {
            readColors();
        }
        return colors[icol % (colors.length)];
    }


    private static void readColors() {
        // TODO - not a great way to load these - shouldn't talk to the loader reallyl
        Object obj = ResourceAccess.getResourceLoader().getResource("StandardColors", null);
        if (obj instanceof StandardColors) {
            StandardColors sc = (StandardColors)obj;

            colors = new Color[colorNames.length];
            for (int i = 0; i < colorNames.length; i++) {
                Color c = sc.getColor(colorNames[i]);
                if (c == null) {
                    E.warning("cant get color " + colorNames[i]);
                } else {
                    colors[i] = c;
                }
            }


        } else {
            E.error("cant read standard colors");
            colors = new Color[1];
            colors[0] = Color.black;

        }
    }
}




