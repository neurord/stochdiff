
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruFlowPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class LeftFlowPanel extends MultiPanel {



    public DruPanel instantiatePanel() {
        DruFlowPanel dfp = new DruFlowPanel(DruFlowPanel.LEFT, xspace, yspace);
        return dfp;
    }

}
