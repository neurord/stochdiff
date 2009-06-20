package org.catacomb.druid.gui.base;

// REFAC - move swing dep;
import javax.swing.ToolTipManager;

public class DruTooltips {



    public static void init() {
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setLightWeightPopupEnabled(false);

        //   E.info("set lightweight off ");
    }

}
