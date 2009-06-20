package org.catacomb.druid.gui.edit;

import org.catacomb.interlish.structure.TargetStore;
import org.catacomb.interlish.structure.TargetStoreUser;
import org.catacomb.interlish.structure.Viewer;
import org.catacomb.report.E;




public class DruMenuButton extends DruButton implements Viewer, TargetStoreUser {
    private static final long serialVersionUID = 1L;


    String popupID;
    TargetStore targetStore;

    DruMenu menu;

    public DruMenuButton(String lab) {
        super(lab);
    }



    public String toString() {
        return ("DruMenuButton  id=" + getID());
    }


    public void labelAction(String s, boolean b) {
        if (menu == null) {
            if (popupID != null && targetStore != null) {
                menu = (DruMenu)(targetStore.get(popupID));
            } else {
                E.error("missing data in menu button " + popupID + " " + targetStore);
            }
        }

        if (menu != null) {
            menu.showPopup(this, 0, 18);
            //menu.getPopupMenu().show(getButton(), 0, 18);
        }
    }


    public void setOptions(String[] sa) {
        if (menu == null) {
            menu = new DruMenu("");
            menu.setItems(sa);
            menu.setActionRelay(getActionRelay());
            menu.setAction(getAction());
        }
    }

    public void setPopupToShow(String show) {
        popupID = show;
    }




    public void setTargetStore(TargetStore ts) {
        targetStore = ts;
    }

}
