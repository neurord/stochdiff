

package org.catacomb.druid.swing;


import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JComponent;

public class DToolablePanel extends DPanel {
    static final long serialVersionUID = 1001;
    CardLayout cardLayout;





    public void addSingle(Object obj) {
        setLayout(new BorderLayout(1, 1)); //GridLayout(1, 1));
        add("Center", (JComponent)obj);
    }


    public void addToolbar(Object tb) {
        if (getLayout() == null) {
            setLayout(new BorderLayout(1, 1));
        }
        add("North", (JComponent)tb);
    }


    public void setCardLayout() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
    }


    public void nextCard() {
        cardLayout.next(this);
    }


    public void showCard(String s) {
        cardLayout.show(this, s);
    }

}
