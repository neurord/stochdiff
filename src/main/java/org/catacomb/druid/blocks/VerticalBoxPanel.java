
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruBoxPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class VerticalBoxPanel extends MultiPanel {


    public DruPanel instantiatePanel() {
        DruBoxPanel dgp = new DruBoxPanel(DruBoxPanel.VERTICAL);
        return dgp;
    }

}
