
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruFlowPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class CenterFlowPanel extends MultiPanel {



    public DruPanel instantiatePanel() {
        return new DruFlowPanel(DruFlowPanel.CENTER, xspace, yspace);
    }

}
