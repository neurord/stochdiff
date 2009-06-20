package org.catacomb.druid.swing.split;

import java.awt.Color;

public class Colors {
    public static Color lightSkyBlue3 = new Color(141, 182, 205);

    public static String getJavaInitializationString(Color c) {
        if (c.equals(Color.black))
            return "java.awt.Color.black";
        else if (c.equals(Color.blue))
            return "java.awt.Color.blue";
        else if (c.equals(Color.cyan))
            return "java.awt.Color.cyan";
        else if (c.equals(Color.darkGray))
            return "java.awt.Color.darkGray";
        else if (c.equals(Color.gray))
            return "java.awt.Color.gray";
        else if (c.equals(Color.green))
            return "java.awt.Color.green";
        else if (c.equals(Color.lightGray))
            return "java.awt.Color.lightGray";
        else if (c.equals(Color.magenta))
            return "java.awt.Color.magenta";
        else if (c.equals(Color.orange))
            return "java.awt.Color.orange";
        else if (c.equals(Color.pink))
            return "java.awt.Color.pink";
        else if (c.equals(Color.red))
            return "java.awt.Color.red";
        else if (c.equals(Color.white))
            return "java.awt.Color.white";
        else if (c.equals(Color.yellow))
            return "java.awt.Color.yellow";
        else
            return "new java.awt.Color("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")";
    }
}