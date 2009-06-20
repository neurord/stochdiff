package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruApplication;
import org.catacomb.druid.gui.base.DruFrame;
import org.catacomb.druid.gui.base.DruTooltips;
import org.catacomb.druid.guimodel.Log;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.interlish.version.BuildInfo;
import org.catacomb.report.E;




import java.util.ArrayList;



public class Application implements AddableTo, Realizer {

    public String name;
    public String versionNumber;
    public String id;
    public String controllerClass;

    public int background;

    // not used here - pull this out before parsing;
    public SplashScreen splashScreen;

    public boolean logging;
    public boolean stateExposure;

    public HTMLContent doc;

    public Frame frame;


    public ArrayList<Requisite> requisites;

    public ArrayList<Dialog> dialogs;

    public ArrayList<Wizard> wizards;


    public Application() {
        requisites = new ArrayList<Requisite>();
        wizards = new ArrayList<Wizard>();
        dialogs = new ArrayList<Dialog>();
    }



    public Object realize(Context ctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;

        DruTooltips.init();

        gpath = gpath.extend(id);

        DruFrame druFrame = (DruFrame)(frame.realize(ctx, gpath));

        druFrame.setTitle(name + " " + BuildInfo.getInfo().getTitleDate());

        DruApplication druapp = new DruApplication();

        druapp.setControllerPath(controllerClass);
        druapp.setName(name);

        druapp.setMainFrame(druFrame);

        if (doc != null) {
            druapp.setDoc(doc.getContent());
        }

        if (dialogs != null) {
            for (Dialog dlg : dialogs) {
                dlg.realize(druFrame, ctx, gpath);
            }
        }


        ctx.addComponent(druapp, gpath);


        BuildInfo bi = BuildInfo.getInfo();
        bi.setName(name);
        bi.setNum(versionNumber);

        bi.printIntro();



        //  druFrame.setTitle(BuildInfo.getInfo().getFrameTitle());


        Log log = new Log("default");
        Log.setSystemLog(log);
        ctx.getMarketplace().global().addReceiver("LogMessage", log, "default");

        if (logging) {
            ctx.getMarketplace().global().addProducer("Log", log, "default");
        }

        if (stateExposure) {
            E.missing("cant do state exposure");
//      ctx.getHookupBoard().addProducer("Page", new StatePageSupplier(doc.getContent()), "state");
        }


        if (requisites != null) {
            for (Requisite requ : requisites) {
                requ.realize(ctx, gpath);
            }
        }

        ctx.getMarketplace().logUnresolved();
        ctx.getMarketplace().global().logUnresolved();

        Log.infoMsg("startup" , BuildInfo.getInfo().getIntro());

        return druapp;
    }



    public void add(Object obj) {
        if (obj instanceof TableTree) {
            E.missing();

        } else if (obj instanceof Requisite) {
            requisites.add((Requisite)obj);

        } else if (obj instanceof Dialog) {
            dialogs.add((Dialog)obj);

        } else if (obj instanceof Wizard) {
            wizards.add((Wizard)obj);

        } else if (obj instanceof Frame) {
            frame = (Frame)obj;

        } else {
            E.error("cant add " + obj);
        }



    }



}
