package org.catacomb.druid.swing;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.Scrollable;


public class DScrollablePanel extends DPanel implements Scrollable {
    private static final long serialVersionUID = 1L;
    CardLayout cardLayout;


    public DScrollablePanel() {
        super();
    }


    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 25;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 25;
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
