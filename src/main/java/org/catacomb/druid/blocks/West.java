package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruBorderPanel;
import org.catacomb.druid.gui.base.DruPanel;


public class West extends PanelInserter {

    public void insert(DruPanel dp, DruBorderPanel container) {
        container.addWest(dp);
    }

}
