
package org.catacomb.druid.swing;


import java.awt.Color;

import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import org.catacomb.interlish.interact.DComponent;



public class DSplitPane extends JSplitPane implements DComponent {
    static final long serialVersionUID = 1001;


    DSplitPaneUI dSplitPaneUI;


    public DSplitPane(int i) {
        super(i);

        dSplitPaneUI = new DSplitPaneUI();
        setUI(dSplitPaneUI);

        setBorder(new EmptyBorder(0, 0, 0, 0));

        setDividerSize(5);

    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }

    public void setBackground(Color c) {
        super.setBackground(c);
        if (dSplitPaneUI != null) {
            dSplitPaneUI.setBackground(c);
        }
    }

}
