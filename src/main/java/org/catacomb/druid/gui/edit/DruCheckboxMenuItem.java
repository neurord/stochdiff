package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DCheckboxMenuItem;
import org.catacomb.interlish.structure.Ablable;
import org.catacomb.interlish.structure.ActionRelay;
import org.catacomb.interlish.structure.ActionSource;
import org.catacomb.interlish.structure.InfoReceiver;
import org.catacomb.interlish.structure.SelectionListener;
import org.catacomb.interlish.structure.SelectionSource;
import org.catacomb.interlish.structure.Syncable;



public class DruCheckboxMenuItem implements ActionSource, LabelActor,
    Ablable, SelectionListener, Syncable {

    static final long serialVersionUID = 1001;

    String label;
    String methodName;
    ActionRelay actionRelay;

    String enableOn;
    SelectionSource selectionSource;

    String info;

    InfoReceiver infoReceiver;

    DCheckboxMenuItem dItem;

    public DruCheckboxMenuItem(String lab, String mnm) {
        dItem = new DCheckboxMenuItem(lab);
        label = lab;
        methodName = mnm;
        dItem.setLabelActor(this);
    }


    public DCheckboxMenuItem getGUIPeer() {
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
            actionRelay.actionB(methodName, b);
        }

    }


    public void setState(boolean b) {
        dItem.setSelected(b);
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
