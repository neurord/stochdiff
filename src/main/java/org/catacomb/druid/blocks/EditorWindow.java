package org.catacomb.druid.blocks;


import org.catacomb.datalish.SColor;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruEditorWindow;
import org.catacomb.druid.gui.base.DruFrame;
import org.catacomb.interlish.resource.Role;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;



import java.util.ArrayList;



public class EditorWindow implements Realizer, AddableTo {

    public String name;
    public String title;
    public String id;
    public String controllerClass;

    public int background;

    public SColor backgroundColor;

    public String closeAction;

    public Frame frame;

    public ArrayList<Dialog> dialogs;




    public EditorWindow() {
    }


    public void add(Object obj) {
        if (obj instanceof Frame) {
            frame = (Frame)obj;


        } else if (obj instanceof Dialog) {
            if (dialogs == null) {
                dialogs = new ArrayList<Dialog>();
            }
            dialogs.add((Dialog)obj);

        } else if (obj instanceof Role) {

        } else {
            E.error("cant add " + obj);
        }
    }


    public Object realize(Context ctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;
        gpath = gpath.extend(id);


        DruFrame druFrame = (DruFrame)(frame.realize(ctx, gpath));

        if (closeAction != null) {
            if (closeAction.equals("hide")) {
                druFrame.setCloseActionHide();
            } else {
                E.warning("unrecognized close action : " + closeAction);
            }
        }

        if (name == null) {
            name = title;
        }

        druFrame.setTitle(name);

        DruEditorWindow druew = new DruEditorWindow();

        if (backgroundColor != null) {
            E.info("need to use bg color");
        }

        druew.setControllerPath(controllerClass);
        druew.setName(name);

        druew.setMainFrame(druFrame);



        ctx.addComponent(druew, gpath);


        if (dialogs != null) {
            for (Dialog d : dialogs) {
                d.realize(ctx, new GUIPath());
            }
        }



        return druew;
    }



}
