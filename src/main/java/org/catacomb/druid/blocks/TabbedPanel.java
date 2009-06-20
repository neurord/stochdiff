
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruTabbedPanel;



public class TabbedPanel extends MultiPanel {


    public DruPanel instantiatePanel() {

        DruTabbedPanel tabp = new DruTabbedPanel();
        return tabp;
    }

}
