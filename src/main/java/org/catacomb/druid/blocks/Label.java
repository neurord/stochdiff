
package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruLabelPanel;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.report.E;


public class Label extends Panel {


    public String text;

    public String align;

    public String fontWeight;

    public String style;



    public DruPanel instantiatePanel() {
        DruLabelPanel drup = new DruLabelPanel(text, align);

        drup.setTitle(text);

        if (style != null && style.toLowerCase().equals("html")) {
            drup.setStyleHTML();
        }

        if (fontWeight != null) {
            if (fontWeight.equals("bold")) {
                drup.setFontBold();
            } else {
                E.warning("unrecognized font weight " + fontWeight);
            }
        }

        return drup;

    }



    @Override
    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {

    }



}
