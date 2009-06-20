
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruProgressReport;


public class ProgressReport extends Panel {


    public String text;


    public DruPanel instantiatePanel() {
        DruProgressReport drup = new DruProgressReport();

        return drup;
    }

    @Override
    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {
        DruProgressReport dpr = (DruProgressReport)drup;
        if (text != null) {
            dpr.setText(text);
        }
    }

}
