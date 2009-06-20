
package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruTextArea;


public class TextArea extends Panel {


    public String store;


    public String action;

    public int width;
    public int height;
    public int rows;

    public boolean editable;

    public boolean antialias;
    public int padding;
    public int fontSize;



    public TextArea() {
        editable = true;
    }


    public DruPanel instantiatePanel() {
        if (rows > height) {
            height = rows;
        }
        return new DruTextArea(action, width, height);
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruTextArea dta = (DruTextArea)dp;

        dta.setEditable(editable);

        if (antialias) {
            dta.setAntialiased();
        }
        if (fontSize > 5) {
            dta.setFontSize(fontSize);
        }

        if (padding > 0) {
            dta.setPadding(padding);
        }

    }

}
