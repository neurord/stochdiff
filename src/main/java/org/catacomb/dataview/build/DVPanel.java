
package org.catacomb.dataview.build;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.report.E;



public class DVPanel {

    public String title;
    public int width;
    public int height;


    public DruPanel makePanel(Context ctxt) {
        E.error("override needed in " + this);
        return null;
    }


}
