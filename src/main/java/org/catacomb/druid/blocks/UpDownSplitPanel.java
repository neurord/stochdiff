
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruUpDownSplitPanel;



public class UpDownSplitPanel extends MultiPanel  {


    public DruPanel instantiatePanel() {
        checkPanelCount(2);
        return new DruUpDownSplitPanel();
    }



}
