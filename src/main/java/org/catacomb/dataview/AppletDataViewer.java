
package org.catacomb.dataview;


import org.catacomb.dataview.build.Dataview;
import org.catacomb.dataview.read.ContentReader;
import org.catacomb.dataview.read.Importer;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.gui.base.DruAppletPrep;
import org.catacomb.interlish.service.Env;
import org.catacomb.report.E;

import java.awt.GridLayout;
import java.net.URL;

import javax.swing.JApplet;


public class AppletDataViewer extends JApplet {
    static final long serialVersionUID = 1001;

    public AppletDataViewer() {
        super();
        Env.setContextApplet();

    }

    public void init() {

        String config = getParameter("config");

        E.info("got config parameter " + config);



        try {
            URL confurl = new URL(getCodeBase(), config);

            ContentReader dsr = Importer.getReader(confurl);


            E.info("should be getting url " + confurl.toString());

            Object obj = dsr.getMain();

            Dataview dv = (Dataview)obj;

            Context ctxt = new Context();


            DruAppletPrep druapp = dv.buildAppletPrep(ctxt);

            getContentPane().setLayout(new GridLayout(1, 1));
            getContentPane().add(druapp.getDruPanel().getGUIPeer());


            DataviewController dvc = new DataviewController(dv);

            //	 druapp.attachController(dvc);

            dvc.setDisplays(ctxt.getCache());

            dvc.initData(dsr);


        } catch (Exception ex) {
            E.error("cant init applet " + ex);
        }

    }

}
