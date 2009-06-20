

package org.catacomb.druid.gui.base;


import org.catacomb.druid.swing.DLabel;


public class DruPlaceholderPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    // String lab;

    public DruPlaceholderPanel(String lab) {
        super();

        // this.lab = lab;

        setFlowLeft(2, 2);



        addDComponent(new DLabel(lab));
    }


}








