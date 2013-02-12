
package org.catacomb.dataview.build;



import org.catacomb.druid.build.Context;
import org.catacomb.druid.gui.base.*;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;

import java.util.ArrayList;

public class Dataview implements AddableTo {


    public int width;
    public int height;

    public DataSource dataSource;

    public FrameSelector frameSelector;

    public String layout;
    public String name;

    public ArrayList<DVPanel> panels;




    public void add(Object obj) {
        if (obj instanceof DVPanel) {
            if (panels == null) {
                panels = new ArrayList<DVPanel>();
            }
            panels.add((DVPanel)obj);

        } else {
            E.debugError("dataview - cannot add " + obj +
                         " (" + obj.getClass().getName() + ")");
        }
    }



    public void show() {

    }


    public DataSource getDataSource() {
        return dataSource;
    }



    public DruApplication buildApplication(Context ctxt) {
        DruApplication druapp = new DruApplication();
        druapp.setName("data viewer");

        DruFrame druf = new DruFrame("data viewer");
        druf.setBackgroundColor(ctxt.getBg());

        druf.setDruPanel(makePanel(ctxt));

        druapp.setMainFrame(druf);

        return druapp;

    }



    public DruAppletPrep buildAppletPrep(Context ctxt) {
        DruAppletPrep druapp = new DruAppletPrep();

        druapp.setDruPanel(makePanel(ctxt));

        return druapp;

    }




    public DruPanel makePanel(Context ctxt) {
        DruBorderPanel dbp = new DruBorderPanel();
        dbp.setBg(ctxt.getBg());
        dbp.setFg(ctxt.getFg());

        DruPanel mainPanel = null;

        int np = panels.size();
        if (np == 1) {
            mainPanel = makeIthPanel(ctxt, 0);
            dbp.addCenter(mainPanel);


        } else {
            DruBoxPanel drubp = null;
            if (layout != null && layout.equals("vertical")) {
                drubp = new DruBoxPanel(DruBoxPanel.VERTICAL, 0);
            } else {
                drubp = new DruBoxPanel(DruBoxPanel.HORIZONTAL, 0);
            }
            drubp.setBg(ctxt.getBg());
            drubp.setFg(ctxt.getFg());


            for (int i = 0; i < panels.size(); i++) {
                DruPanel dp = makeIthPanel(ctxt, i);
                drubp.addPanel(dp);
            }
            dbp.addCenter(drubp);
            mainPanel = drubp;
        }


        mainPanel.setID("main");
        ctxt.addToCache(mainPanel);

        if (frameSelector != null) {
            DruPanel drufp = frameSelector.makePanel(ctxt);
            dbp.addSouth(drufp);
        }

        dbp.addBorder(6, 6, 6, 6);
        return dbp;
    }



    private DruPanel makeIthPanel(Context ctxt, int ipan) {
        DVPanel dvp = panels.get(ipan);

        DruPanel drup = dvp.makePanel(ctxt);
        drup.addBorder(4, 4, 4, 4);
        return drup;
    }



}
