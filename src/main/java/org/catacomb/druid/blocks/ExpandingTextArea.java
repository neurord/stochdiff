
package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruExpandingTextArea;


public class ExpandingTextArea extends TextArea {

    public boolean autoResize = false;

    public DruPanel instantiatePanel() {
        return new DruExpandingTextArea(action, width, height);
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruExpandingTextArea dta = (DruExpandingTextArea)dp;

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

        if (autoResize) {
            dta.setAutoResize(true);
        }

    }

}
