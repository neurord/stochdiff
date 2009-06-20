

package org.catacomb.druid.gui.base;



public class DruCollapsiblePanel extends DruPanel {
    static final long serialVersionUID = 1001;


    DruPanel contentPanel;
    boolean collapsed;


    public DruCollapsiblePanel() {
        super();
    }


    public void addContentPanel(DruPanel drup) {
        contentPanel = drup;
        // super.addPanel(contentPanel);
        collapsed = true;
    }

    public void collapse() {
        if (collapsed) {

        } else {
            removePanel(contentPanel);
            collapsed = true;
        }
    }

    public void expand() {
        if (collapsed) {
            addPanel(contentPanel);
            collapsed = false;
        } else {
            // nothing to do;
        }
    }



}








