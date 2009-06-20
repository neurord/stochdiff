
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruBoxPanel;
import org.catacomb.druid.gui.base.DruFloatBoxPanel;
import org.catacomb.druid.gui.base.DruPanel;




public class FloatBoxPanel extends MultiPanel {


    public DruPanel instantiatePanel() {
        return new DruFloatBoxPanel(DruBoxPanel.VERTICAL);
    }


}
