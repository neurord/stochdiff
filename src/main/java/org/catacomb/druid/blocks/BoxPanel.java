
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruBoxPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class BoxPanel extends MultiPanel {

    public String alignment;


    public DruPanel instantiatePanel() {

        return new DruBoxPanel(DruBoxPanel.VERTICAL);
    }



}
