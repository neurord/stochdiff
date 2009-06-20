package org.catacomb.druid.gui.base;

import org.catacomb.druid.event.ClosureListener;
import org.catacomb.druid.swing.DFrame;
import org.catacomb.interlish.structure.ActionRelay;
import org.catacomb.interlish.structure.ActionSource;
import org.catacomb.report.E;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;


public class DruFrame implements ActionSource, ClosureListener  {
    static final long serialVersionUID = 1001;

    DruPanel druPanel;

    String id;

    ActionRelay actionRelay;

    boolean donePack;

    boolean hideOnClose;

    DFrame dFrame;

    Color backgroundColor;


    public DruFrame(String s) {
        dFrame = new DFrame(s);
        dFrame.setClosureListener(this);
        donePack = false;
        hideOnClose = false;
    }

    public DFrame getGUIPeer() {
        return dFrame;
    }


    public void setPreferredSize(int w, int h) {
        dFrame.setPreferredSize(w, h);
    }

    public String toString() {
        return "DruFrame " + id;
    }

    public void pack() {
        donePack = true;
        dFrame.pack();
    }

    public void packIfNecessary() {
        if (!donePack) {
            pack();
        }
    }



    public void setID(String s) {
        id = s;
    }


    public String getID() {
        return id;
    }


    public void setCloseActionHide() {
        hideOnClose = true;
    }


    public void requestClose() {
        if (hideOnClose) {
            dFrame.setVisible(false);

        } else if (actionRelay == null) {
            E.error("no action connector for this frame " + id);
            dFrame.setVisible(true);

        } else {
            actionRelay.action("requestClose");
        }
    }


    public void closed() {
        System.out.println("frame closed");
    }



    public void setActionRelay(ActionRelay ar) {
        actionRelay = ar;
    }



    public void setDruMenuBar(DruMenuBar amb) {
        dFrame.setJMenuBar(amb.getGUIPeer());
    }


    public void setDruPanel(DruPanel axp) {
        druPanel = axp;
        if (backgroundColor != null) {
            druPanel.setFallbackBackgroundColor(backgroundColor);
        }
        dFrame.getContentPane().add("Center", axp.getGUIPeer());
    }


    public void setVisible(boolean b) {
        dFrame.setVisible(b);
    }


    public void dispose() {
        dFrame.dispose();
    }


    public void setBackgroundColor(Color bg) {
        backgroundColor = bg;
        dFrame.setBg(bg);
    }


    public void setTitle(String s) {
        dFrame.setTitle(s);
    }


    public int[] getIntArraySize() {
        return dFrame.getIntArraySize();
    }

    public int[] getLocation() {
        Point p = dFrame.getLocation();
        int[] ret = {(int)(p.getX()), (int)(p.getY())};
        return ret;
    }

    public void setLocation(int x, int y) {
        dFrame.setLocation(x, y);
    }

    public void toFront() {
        dFrame.toFront();

    }

    public void waitCursor() {
        dFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public void normalCursor() {
        dFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public Object getContent() {
        return dFrame.getContentPane();
    }



}
