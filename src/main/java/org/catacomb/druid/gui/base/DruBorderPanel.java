package org.catacomb.druid.gui.base;

import javax.swing.JPanel;

import org.catacomb.druid.swing.DBorderLayout;




public class DruBorderPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    public DruBorderPanel() {
        super();

        setBorderLayout(0, 0);

    }

    public DruBorderPanel(int xspace, int yspace) {
        super();
        setBorderLayout(xspace, yspace);

    }


    public DruBorderPanel(int props, int xspace, int yspace) {
        super(props);
        setBorderLayout(xspace, yspace);

    }


    public void addNorth(DruPanel dp) {
        addPanel(dp, DBorderLayout.NORTH);
    }

    public void addSouth(DruPanel dp) {
        addPanel(dp, DBorderLayout.SOUTH);
    }

    public void addEast(DruPanel dp) {
        addPanel(dp, DBorderLayout.EAST);
    }

    public void addWest(DruPanel dp) {
        addPanel(dp, DBorderLayout.WEST);
    }

    public void addCenter(DruPanel dp) {
        addPanel(dp, DBorderLayout.CENTER);
    }



    public void addNorth(DruLabel dp) {
        setColors(dp);
        addDComponent(dp.getGUIPeer(), DBorderLayout.NORTH);
    }

    public void addSouth(DruLabel dp) {
        setColors(dp);
        addDComponent(dp.getGUIPeer(), DBorderLayout.SOUTH);
    }

    public void addEast(DruLabel dp) {
        setColors(dp);
        addDComponent(dp.getGUIPeer(), DBorderLayout.EAST);
    }

    public void addWest(DruLabel dp) {
        setColors(dp);
        addDComponent(dp.getGUIPeer(), DBorderLayout.WEST);
    }

    public void addCenter(DruLabel dp) {
        setColors(dp);
        addDComponent(dp.getGUIPeer(), DBorderLayout.CENTER);
    }


    public static Object getCenterConstraints() {
        return DBorderLayout.CENTER;
    }

    public void addRaw(JPanel panel) {
        addRaw(panel, DBorderLayout.CENTER);
    }




}
