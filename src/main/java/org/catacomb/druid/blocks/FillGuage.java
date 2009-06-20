
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruFillGuage;


public class FillGuage extends Panel {

    public double value;

    public String text;




    public DruPanel instantiatePanel() {
        DruFillGuage drup = new DruFillGuage(value, text);

        return drup;
    }




    @Override
    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {

    }

}
