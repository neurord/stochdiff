
package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DCardPanel;

public class DruCardPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    DCardPanel dCardPanel;


    public DruCardPanel() {
        dCardPanel = new DCardPanel();
        dPanel = dCardPanel;
    }



    public void nextCard() {
        dCardPanel.nextCard();
    }


    public void addPanel(DruPanel drup) {
        setColors(drup);
        addCardPanel(drup);
    }


    public void showCard(String s) {
        ((DCardPanel)getGUIPeer()).showCard(s);
    }






}
