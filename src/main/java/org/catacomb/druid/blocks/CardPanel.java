
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruCardPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class CardPanel extends MultiPanel {




    public DruPanel instantiatePanel() {
        return new DruCardPanel();
    }


}
