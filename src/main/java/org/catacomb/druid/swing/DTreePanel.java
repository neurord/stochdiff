
package org.catacomb.druid.swing;


import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTree;

public class DTreePanel extends JPanel {
    static final long serialVersionUID = 1001;

    JTree jTree;

    public DTreePanel(String s) {
        super();

        Color col = LAF.getBackgroundColor();

        //      System.out.println("panel setting background " + col);

        setBackground(col);

        jTree = new JTree();
        setLayout(new GridLayout(1, 1));
        add(jTree);


    }



    /*
    public void addBorder(int l, int r, int t, int b) {
       System.out.println("DPanel adding border " + l + " " + r );

       Border border = BorderFactory.createEmptyBorder(t, l, b, r);
       setBorder(border);
    }
    */

}
