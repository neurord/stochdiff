package org.catacomb.druid.gui.base;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.FlatGUIPath;
import org.catacomb.druid.build.PanelFactory;
import org.catacomb.druid.build.Realizer;
import org.catacomb.interlish.structure.IDd;
import org.catacomb.report.E;


import java.util.HashMap;


public class DruKitPanel extends DruPanel implements PanelFactory {
    private static final long serialVersionUID = 1L;


    HashMap<String, Realizer> realizers;

    Context context;

    HashMap<String, PanelPack> singletons;

    public DruKitPanel() {
        realizers = new HashMap<String, Realizer>();
        singletons = new HashMap<String, PanelPack>();
    }


    public void addRealizer(Realizer rlz) {
        if (rlz instanceof IDd) {
            String sid = ((IDd)rlz).getID();
            if (sid == null) {
                E.error("each top level item in a kit panel must have an id");
            } else {
                realizers.put(sid, rlz);
            }
        } else {
            E.error("kit panel can only add idd cpts - not " + rlz);
        }
    }


    public void setRealizationContext(Context ctx) {
        context = ctx;

        if (getInfoReceiver() == null) {

        } else {
            E.warning("resetting ir " + getInfoReceiver() + " " + ctx.getInfoAggregator());
        }

        setInfoReceiver(ctx.getInfoAggregator());
    }



    public boolean canMake(String sid) {
        return (realizers.containsKey(sid));
    }


    public PanelPack newPanelPack(String sid) {
        PanelPack ret = null;
        if (realizers.containsKey(sid)) {

            Realizer rlz = realizers.get(sid);
            context.resetStore();
            DruPanel drup = (DruPanel)(rlz.realize(context, new FlatGUIPath()));

            ret = new PanelPack(drup,
                                context.getAnonymousComponents(),
                                context.getIdentifiedComponents());

        } else {
            E.error("no such item in kit " + sid);
            for (String s : realizers.keySet()) {
                E.info("known item: " + s);
            }
        }
        return ret;
    }

    public PanelPack getSingletonPack(String sid) {
        PanelPack ret = null;
        if (singletons.containsKey(sid)) {
            ret = singletons.get(sid);
        } else {
            ret = newPanelPack(sid);
            //        E.info("created a new singleton called " + sid + " within " + this + " " + context);
            singletons.put(sid, ret);
        }
        return ret;
    }


    public DruPanel showPanel(String sid, Object dest) {
        removeAll();
        PanelPack ppk = getSingletonPack(sid);
        ppk.actionConnect(dest);
        DruPanel dp = ppk.getMainPanel();
        addPanel(dp);
        return dp;
    }


}
