package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruBorderPanel;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


import java.util.ArrayList;


public class BorderPanel extends Panel implements AddableTo {


    public int xspace;
    public int yspace;


    ArrayList<PanelInserter> inserters;


    public BorderPanel() {
        xspace = 0;
        yspace = 0;
    }


    public void add(Object obj) {
        if (inserters == null) {
            inserters = new ArrayList<PanelInserter>();
        }
        if (obj instanceof PanelInserter) {
            inserters.add((PanelInserter)obj);

        } else {
            E.error("cannot add " + obj);
        }
    }





    public DruPanel instantiatePanel() {
        return new DruBorderPanel(xspace, yspace);
    }



    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {
        if (inserters != null) {
            for (PanelInserter pi : inserters) {
                DruPanel dp = pi.realizePanel(ctx, gpath);
                if (pi == null) {
                    E.error("null panel from inserter " + pi);
                } else if (dp == null) {
                    // OK - nothing was specified to go in the panel;

                } else {
                    pi.insert(dp, (DruBorderPanel)drup);
                }
            }
        }

    }

}
