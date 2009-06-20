package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruScrollableBorderPanel;


public class ScrollableBorderPanel extends BorderPanel {




    public DruPanel instantiatePanel() {
        return new DruScrollableBorderPanel(xspace, yspace);
    }


}
