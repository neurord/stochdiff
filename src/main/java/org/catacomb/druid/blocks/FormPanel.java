
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruFormPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class FormPanel extends MultiPanel {



    public DruPanel instantiatePanel() {

        return  new DruFormPanel();
    }

}
