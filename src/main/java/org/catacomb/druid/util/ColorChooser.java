package org.catacomb.druid.util;

import org.catacomb.druid.swing.DColorChooser;

import java.awt.Color;

public class ColorChooser {

    @SuppressWarnings("unused")
    public static Color getColor(String s, Color col) {
        return DColorChooser.showDialog(null, "color", col);
    }

}
