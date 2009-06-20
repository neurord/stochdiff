package org.catacomb.druid.gui.base;


import org.catacomb.interlish.structure.IDable;


public class DruAutonomousPanel extends DruRoot
    implements PanelWrapper, IDable {


    DruPanel mainPanel;


    String id; // this is the ide seen by the parent app - used to access this panel



    public void setID(String s) {
        id = s;
    }


    public String getID() {
        return id;
    }


    public void setMainPanel(DruPanel dp) {
        mainPanel = dp;
    }


    public DruPanel getPanel() {
        return mainPanel;
    }


}
