
package org.catacomb.druid.blocks;


import org.catacomb.druid.gui.base.DruLeftRightSplitPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class LeftRightSplitPanel extends MultiPanel {




    public DruPanel instantiatePanel() {
        checkPanelCount(2);
        return new DruLeftRightSplitPanel();
    }





}
