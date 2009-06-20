package org.catacomb.druid.swing;


import org.catacomb.druid.event.PanelListener;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.report.E;
import org.catacomb.util.ColorUtil;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class DTabbedPane extends JTabbedPane implements DComponent, ChangeListener {

    static final long serialVersionUID = 1001;


    public PanelListener pListener;

    int ntab;
    String[] tabNames;

    boolean dontReport;


    public DTabbedPane() {
        super();
        dontReport = false;
        addChangeListener(this);

        ntab = 0;
        tabNames = new String[20];
    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }


    public static void applyUIColors(Color c) {
        // Color cbr = c.brighter();
        // Color cdk = c.darker();
        // Color cdkdk = cdk.darker();


        Color cbr = ColorUtil.slightlyBrighter(c);
        Color cdk = ColorUtil.slightlyDarker(c);
        Color cdkdk = ColorUtil.slightlyDarker(cdk);

        UIManager.put("TabbedPane.contentAreaColor", c);
        UIManager.put("TabbedPane.selected", c);
        UIManager.put("TabbedPane.selectedBackground", c);
        UIManager.put("TabbedPane.unselectedBackground", cdk);

        UIManager.put("TabbedPane.tabAreaBackground", c);
        UIManager.put("TabbedPane.selectHighlight", cbr);
        UIManager.put("TabbedPane.tabsOpaque", new Boolean(false));
        UIManager.put("TabbedPane.borderHightlightColor", cdk);

        UIManager.put("TabbedPane.light", cbr);
        UIManager.put("TabbedPane.highlight", cbr);
        UIManager.put("TabbedPane.shadow", cdk);
        UIManager.put("TabbedPane.darkShadow", cdkdk);
        UIManager.put("TabbedPane.focus", c);


    }



    /*
     * public Color getBackgroundAt(int index) { if (index == 0) {
     * //getSelectedIndex()) { return Color.magenta; } else { return
     * super.getBackgroundAt(index).darker(); } }
     *
     * public void updateUI() { setUI(new BasicTabbedPaneUI()); }
     *
     */

    public void select(Object obj) {
        setSelectedComponent((Component)obj);

        report();
    }


    private void report() {
        if (dontReport) {

        } else {
            if (pListener != null) {
                Object obj = getSelectedComponent();
                pListener.panelShown(obj);
            }
        }
    }


    public void setBg(Color c) {
        setBackground(c);
    }


    public void setFg(Color c) {
        setForeground(c);
    }


    public void setPanelListener(PanelListener pl) {
        pListener = pl;
    }


    public void stateChanged(ChangeEvent cev) {
        report();
    }


    public void addTab(String s, Component c) {
        super.addTab(s, c);
        E.error("shouldn't use");
    }


    public void addTab(String s, Component c, String tooltip) {
        dontReport = true;

        super.addTab(s, null, c,  tooltip);
        setBackgroundAt(ntab, null);
        tabNames[ntab++] = s;

        dontReport = false;
    }


    public void showTab(String s) {
        boolean done = false;
        for (int i = 0; i < ntab; i++) {
            if (tabNames[i].equals(s)) {
                done = true;
                setSelectedIndex(i);
            }
        }
        if (!done) {
            E.error("no such tab " + s);
            for (int i = 0; i < ntab; i++) {
                E.info("tab " + i + " has name " + tabNames[i]);
            }
        }
    }



}
