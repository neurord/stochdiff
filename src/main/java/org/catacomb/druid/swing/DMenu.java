
package org.catacomb.druid.swing;

import java.awt.Font;

import javax.swing.JMenu;

import javax.swing.JComponent;

public class DMenu extends JMenu {
    static final long serialVersionUID = 1001;

    public DMenu(String s) {
        super(s);

        setFont(new Font("sansserif", Font.PLAIN, 12));

        setBackground(LAF.getBackgroundColor());
    }


    public void preShowSync() {

    }


    public void applyRollover() {
        addMouseListener(new RolloverEffect(this,
                                            RolloverEffect.NONE,
                                            RolloverEffect.ETCHED_UP));
    }


    public void showPopup(JComponent jcpt, int i, int j) {
        getPopupMenu().show(jcpt, i, j);

    }

}
