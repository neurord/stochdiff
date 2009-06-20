
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruBoxPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class HorizontalBoxPanel extends MultiPanel {


    public DruPanel instantiatePanel() {
        return new DruBoxPanel(DruBoxPanel.HORIZONTAL);
    }

}
