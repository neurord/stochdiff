package org.catacomb.druid.swing;


// for the laf
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;




public class DSplitPaneUI extends BasicSplitPaneUI {

    JComponent jcomponent;

    Color bgColor = Color.gray;



    DSplitPaneUI() {
        this(null);
    }


    DSplitPaneUI(JComponent jc) {
        super();
        jcomponent = jc;
    }



    public Color getBackground() {
        return bgColor;
    }


    public void setBackground(Color c) {
        bgColor = c;
    }



    public static ComponentUI createUI(JComponent jcomponent) {
        return (new DSplitPaneUI(jcomponent));
    }



    public BasicSplitPaneDivider createDefaultDivider() {
        return (new DSplitPaneDivider(this));
    }


    public void paint(Graphics graphics, JComponent jc) {
    }


    protected void uninstallDefaults() {
    }

}


