package org.catacomb.druid.chunk;

import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.content.BooleanValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;

import java.util.ArrayList;


public class MouseModeController implements Controller, ModeController {


    ArrayList<ModeSettable> modeSettables = new ArrayList<ModeSettable>();


    @Editable(xid="antialias")
    public BooleanValue antialiasBV;

    @Editable(xid="labels")
    public BooleanValue labelsBV;


//   @Editable(guiID="modeToggle")
//   public StringValue mouseModeSV;


    public MouseModeController() {
        antialiasBV = new BooleanValue(false);
        labelsBV = new BooleanValue(false);

    }


    public void addModeSettable(ModeSettable ms) {
        modeSettables.add(ms);
        setLabels(labelsBV.getBoolean());

    }


    public void attached() {
    }


    public void reframe() {
        for (ModeSettable ms : modeSettables) {
            ms.setMode("showAll", true);
        }
    }


    public void setAntialias(boolean b) {
        if (b == antialiasBV.getBoolean()) {
            for (ModeSettable ms : modeSettables) {
                ms.setMode("antialias", b);
            }
        } else {
            E.error("out of sync");
        }
    }



    public void setLabels(boolean b) {
        if (b == labelsBV.getBoolean()) {
            for (ModeSettable ms : modeSettables) {
                ms.setMode("labels", b);
            }

        } else {
            E.error("out of sync");
        }
    }


    private void exportMode(String mode, String val) {
        for (ModeSettable ms : modeSettables) {
            ms.setMode(mode, val);
        }
    }


    public void setMouseMode(String s) {
        E.info("mouse mode panel setting mode " + s);
        exportMode("mouse", s);
    }

    public void setPan(boolean b) {
        exportMode("mouse", "pan");
    }
    public void setZoomIn(boolean b) {
        exportMode("mouse", "zoomIn");
    }
    public void setZoomOut(boolean b) {
        exportMode("mouse", "zoomOut");
    }
    public void setBox(boolean b) {
        exportMode("mouse", "box");
    }
    public void setMulti(boolean b) {
        exportMode("mouse", "all");
    }


}