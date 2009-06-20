
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruMultiSplitPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class VerticalMultiSplitPanel extends MultiPanel {


    public DruPanel instantiatePanel() {
        return new DruMultiSplitPanel(DruMultiSplitPanel.VERTICAL);
    }




}
