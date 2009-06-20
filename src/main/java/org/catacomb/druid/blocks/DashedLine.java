
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruDotsPanel;
import org.catacomb.druid.gui.base.DruPanel;


public class DashedLine extends Panel {


    public String text;

    public String align;

    public String fontweight;




    public DruPanel instantiatePanel() {
        return new DruDotsPanel();
    }

    public void populatePanel(DruPanel dp,Context ctx, GUIPath gpath) {
    }



}
