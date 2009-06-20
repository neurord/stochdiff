package org.catacomb.druid.dialog;

import org.catacomb.druid.build.Druid;
import org.catacomb.interlish.structure.*;



public abstract class DialogController implements Controller {

    private Druid druid;


    public DialogController() {
        super();

    }


    public void checkInit() {
        if (druid == null) {
            druid = new Druid(getDialogDeclaration());
            druid.buildGUI();
            druid.attachSingleController(this);
        }
    }

    public String getDialogDeclaration() {
        String scn = getClass().getName();
        // String scn = fcn.substring(fcn.lastIndexOf(".") + 1, fcn.length());
        if (scn.endsWith("Controller")) {
            scn = scn.substring(0, scn.indexOf("Controller"));
        }
        return scn;
    }



    public void attached() {
    }

    private boolean isShowing() {
        return druid.isShowing();
    }
    @SuppressWarnings("unused")
    public void show(Object obj) {
    }


    public void hideDialog() {
        checkInit();
        druid.hide();

    }


    public void showModalAt(int x, int y) {
        checkInit();
        druid.setModal(true);
        if (isShowing()) {
            show();
        } else {
            showAt(x, y);
        }
    }



    public void showNonModalAt(int x, int y) {
        checkInit();
        druid.setModal(false);

        if (isShowing()) {
            show();
        } else {
            showAt(x, y);
        }
    }

    protected void show() {
        FrameShowable fs = druid.getFrameShowable();
        fs.pack();
        fs.show();
        fs.toFront();
    }


    protected void showAt(int x, int y) {
        checkInit();
        FrameShowable fs = druid.getFrameShowable();
        fs.pack();
        //int[] wh = fs.getSize();
        fs.setLocation(x, y);
//        fs.setLocation(x - wh[0] / 2, y - wh[1] + 20);
        fs.show();
        fs.toFront();
    }


}
