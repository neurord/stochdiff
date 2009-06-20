
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruPlaceholderPanel;


public class Placeholder extends Panel {


    public String text;



    public DruPanel instantiatePanel() {
        DruPlaceholderPanel drup = new DruPlaceholderPanel(text);
        return drup;
    }



    @Override
    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {

    }

}
