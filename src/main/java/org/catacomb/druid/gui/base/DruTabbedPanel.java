
package org.catacomb.druid.gui.base;



import org.catacomb.druid.event.PanelListener;
import org.catacomb.druid.swing.DTabbedPane;
import org.catacomb.report.E;

import java.awt.Color;

import java.util.HashMap;


public class DruTabbedPanel extends DruPanel implements PanelListener {
    static final long serialVersionUID = 1001;

    DTabbedPane dTabbedPane;

    HashMap<Object, DruPanel> panelsDtoDru;

    public DruTabbedPanel() {

        super();

        panelsDtoDru = new HashMap<Object, DruPanel>();

        dTabbedPane = new DTabbedPane();
        setGridLayout(1, 1, 2, 2);
        addDComponent(dTabbedPane);

        dTabbedPane.setPanelListener(this);
    }


    @SuppressWarnings("unused")
    public DruTabbedPanel(String s) {
        this();
    }


    public static void applyUIColors(Color c) {
        DTabbedPane.applyUIColors(c);
    }


    public void panelShown(Object obj) {
        if (panelsDtoDru.containsKey(obj)) {
            DruPanel drup = panelsDtoDru.get(obj);
            drup.exportInfo();

        } else {
            E.info("dru tabbed panel called panelShown on unknown" + obj);
        }
    }


    public void setBg(Color c) {
        dTabbedPane.setBg(c);
        super.setBg(c);
    }

    public void setFg(Color c) {
        dTabbedPane.setFg(c);
        super.setFg(c);
    }



    public void addPanel(DruPanel axp) {
        panelsDtoDru.put(axp.getGUIPeer(), axp);

        dTabbedPane.addTab(axp.getTitle(),  axp.getGUIPeer(), axp.getTip());
    }



    public void showTab(String s) {
        dTabbedPane.showTab(s);

    }

}

