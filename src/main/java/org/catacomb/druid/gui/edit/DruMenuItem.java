package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DMenuItem;
import org.catacomb.interlish.structure.*;



public class DruMenuItem implements ActionSource, LabelActor,
    Ablable, SelectionListener, Syncable {

    static final long serialVersionUID = 1001;

    String label;
    String methodName;
    ActionRelay actionRelay;

    String enableOn;
    SelectionSource selectionSource;

    String info;

    InfoReceiver infoReceiver;

    DMenuItem dItem;

    public DruMenuItem(String lab, String mnm) {
        dItem = new DMenuItem(lab);
        label = lab;
        methodName = mnm;
        dItem.setLabelActor(this);
    }


    public DMenuItem getGUIPeer() {
        return dItem;
    }

    public String getID() {
        return "";
    }


    public void able(boolean b) {
        dItem.setEnabled(b);
    }




    public void setActionRelay(ActionRelay ar) {
        actionRelay = ar;
    }



    public void labelAction(String s, boolean b) {
        if (info != null) {
            infoReceiver.receiveInfo(label, info);
        }
        if (actionRelay != null) {
            actionRelay.action(methodName);
        }

    }


    public void setEnableOnSelection(String depends) {
        enableOn = depends;
    }


    public void setSelectionSource(SelectionSource source) {
        selectionSource = source;
    }


    public void sync() {
//      E.info("dmi syncing  enable=" + enableOn);
        if (selectionSource != null && enableOn != null) {
            String s = selectionSource.getSelectionType();
            if (enableOn.indexOf(s) >= 0) {
                dItem.setEnabled(true);
            } else {
                dItem.setEnabled(false);
            }

        }
    }


    public void setInfo(String s) {
        info = s;
    }


    public void setInfoReceiver(InfoReceiver irec) {
        infoReceiver = irec;
    }


}
