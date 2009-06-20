package org.catacomb.druid.swing;

import java.awt.CardLayout;


public class DCardPanel extends DPanel {
    private static final long serialVersionUID = 1L;


    CardLayout cardLayout;

    public DCardPanel() {
        super();
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
