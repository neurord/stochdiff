
package org.catacomb.druid.swing;

import java.awt.Font;

import javax.swing.JMenu;


public class DSubMenu extends JMenu {
    static final long serialVersionUID = 1001;


    public DSubMenu(String s) {
        super(s);

        setFont(new Font("sansserif", Font.PLAIN, 12));

        setBackground(LAF.getBackgroundColor());
    }


}
