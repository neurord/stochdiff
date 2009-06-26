package org.catacomb.druid.gui.base;


import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.interlish.interact.Workbench;
import org.catacomb.interlish.structure.AssemblyEditor;
import org.catacomb.interlish.structure.ModeController;
import org.catacomb.report.E;


import java.awt.Color;



public class DruAssemblyPanel extends DruBorderPanel implements AssemblyEditor {
    static final long serialVersionUID = 1001;

    Workbench workbench;

    int prefw;
    int prefh;

    Color bgc;
    Color canvasColor;
    Color shelfColor;
    ModeController modeController;


    public DruAssemblyPanel(int w, int h, String bsName) {
        super();
        prefw = w;
        prefh = h;
    }


    public void setWorkbench(Workbench wkb) {
        workbench = wkb;
        workbench.setPreferredSize(prefw, prefh);
        addDComponent(workbench, DBorderLayout.CENTER);
        applyToWorkbench();
    }


    private void applyToWorkbench() {
        if (bgc != null) {
            workbench.setBg(bgc);
            workbench.setSunkenBorder(bgc);
        }
        if (modeController != null) {
            workbench.setModeController(modeController);
        }
        if (canvasColor != null) {
            workbench.setCanvasColor(canvasColor);
        }
        if (shelfColor != null) {
            workbench.setShelfColor(shelfColor);
        }
        if (infoReceiver != null) {
            workbench.setInfoReceiver(infoReceiver);
        }
    }



    public void setBg(Color c) {
        super.setBg(c);
        bgc = c;
        if (workbench != null) {
            workbench.setBg(c);
            workbench.setSunkenBorder(c);
        }
    }

    public void postApply() {
        if (workbench != null) {
            applyToWorkbench();
        }
    }


    public void setModeController(ModeController mc) {
        modeController = mc;
        if (workbench != null) {
            workbench.setModeController(mc);
        }
    }




    public void setAssembly(Object ass) {
        if (workbench != null) {
            workbench.setAssembly(ass);
        } else {
            E.error("must have workbench before setting assembly");
        }
    }

    public Workbench getWorkbench() {
        return workbench;
    }


    public void setCanvasColor(Color color) {
        canvasColor = color;
        if (workbench != null) {
            workbench.setCanvasColor(color);
        }
    }

    public void setShelfColor(Color color) {
        shelfColor = color;
        if (workbench != null) {
            workbench.setShelfColor(color);
        }
    }

}