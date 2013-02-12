
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruKitPanel;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.interlish.structure.AdderTo;
import org.catacomb.report.E;


import java.util.ArrayList;

public class KitPanel extends Panel implements AddableTo {

    public ArrayList<Realizer> realizers;

    public KitPanel() {
    }

    public void add(Object obj) {
        if (realizers == null) {
            realizers = new ArrayList<Realizer>();
        }
        if (obj instanceof Realizer) {
            realizers.add((Realizer)obj);

        } else if (obj instanceof AdderTo) {   // only for ChildrenOf - mention explicitly?
            ((AdderTo)obj).addTo(this);

        } else {
            E.error("cannot add non-realizer " + obj + " to " + this);
        }
    }


    public DruPanel instantiatePanel() {
        return new DruKitPanel();
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruKitPanel dkp = (DruKitPanel)dp;


        dkp.setRealizationContext(ctx.simpleCopy());

        for (Realizer rlz : realizers) {
            dkp.addRealizer(rlz);
        }

    }




}
