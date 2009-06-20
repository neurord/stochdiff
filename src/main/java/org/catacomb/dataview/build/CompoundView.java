
package org.catacomb.dataview.build;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;

import java.util.ArrayList;




public class CompoundView extends DVPanel implements AddableTo {

    public String layout;


    public ArrayList<DVPanel> panels;




    public void add(Object obj) {
        if (obj instanceof DVPanel) {
            if (panels == null) {
                panels = new ArrayList<DVPanel>();
            }
            panels.add((DVPanel)obj);

        } else {
            E.error("cant add " + obj + " toi compound view");
        }
    }



    public DruPanel makePanel(Context ctxt) {
        E.error(" CompoundView is redundant ??");
        return null;

    }

}
