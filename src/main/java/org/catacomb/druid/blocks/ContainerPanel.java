
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;


public class ContainerPanel extends Panel {


    public String text;



    public DruPanel instantiatePanel() {
        DruPanel drup = new DruPanel();
        return drup;
    }



    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {

    }

}
