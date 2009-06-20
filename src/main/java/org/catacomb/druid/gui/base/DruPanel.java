package org.catacomb.druid.gui.base;


import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.druid.swing.DPanel;
import org.catacomb.druid.swing.DScrollablePanel;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.InfoReceiver;
import org.catacomb.interlish.structure.MouseActor;
import org.catacomb.report.E;


import java.awt.Color;
import java.awt.Point;

import java.awt.Dimension;

import javax.swing.JPanel;

public class DruPanel implements MouseActor {
    static final long serialVersionUID = 1001;

    public static final int SCROLLABLE = 1;

    String id;
    String title;
    String tip;
    String info;

    DPanel dPanel;

    public InfoReceiver infoReceiver;

    Color bgColor;
    Color fgColor;

    DComponent tooltipTarget;

    public DruPanel() {
        dPanel = new DPanel();
        id = "";
    }

    public DruPanel(int props) {
        if (props == SCROLLABLE) {
            dPanel = new DScrollablePanel();
        } else {
            dPanel = new DPanel();
        }
        id = "";
    }


    public void setTooltipTarget(DComponent dc) {
        tooltipTarget = dc;
    }


    public DPanel getGUIPeer() {
        return dPanel;
    }

    public String toString() {
        return getClass().getName() + " id=" + id;
    }


    public int[] getLocation() {
        Point p = getGUIPeer().getLocation();
        int[] ret = {(int)(p.getX()), (int)(p.getY())};
        return ret;
    }


    public void setID(String s) {
        id = s;
    }


    public String getID() {
        return id;
    }


    public void setTitleInfo(String st, String si) {
        title = st;
        info = si;
    }


    public void setTitle(String s) {
        if (title != null) {
            E.warning("overwriting title " + title + " with " + s);
        }
        title = s;
    }

    public void setTip(String s) {

        tip = s;
        if (tip != null && tip.length() > 3) {
            if (tooltipTarget != null) {
                tooltipTarget.setTooltip(tip);
            } else {
                E.shortWarning("no target for tooltip " + s + " " + this);
            }
        }
    }

    public String getTip() {
        return tip;
    }


    public String getTitle() {
        return title;
    }



    public void setInfo(String s) {
        info = s;
    }


    public String getInfo() {
        return info;
    }


    public void setInfoReceiver(InfoReceiver ir) {
        infoReceiver = ir;
    }

    public InfoReceiver getInfoReceiver() {
        return infoReceiver;
    }


    public void mouseButtonPressed() {
        exportInfo();
    }



    public void exportInfo() {
        if (infoReceiver != null) {

            if (getInfo() != null && getInfo().length() > 0) {
                if (getTitle() == null || getTitle().startsWith("Panel")) {
                    E.shortWarning("no title for panel exporting info " + this);
                }

                infoReceiver.receiveInfo(getTitle(), getInfo());
            }

        } else {
            E.warning("no info receiver on " + this);
            (new Exception()).printStackTrace();
        }
    }


    public void setBg(Color c) {
        bgColor = c;
        dPanel.setBg(c);
    }

    public void setFg(Color c) {
        fgColor = c;
        dPanel.setFg(c);
    }

    public Color getBg() {
        return bgColor;
    }

    public void setColors(DruPanel dp) {
        dp.setFallbackBackgroundColor(bgColor);
        dp.setFallbackForegroundColor(fgColor);
    }


    public void setColors(DruLabel dp) {
        dp.setBg(bgColor);
        dp.setFg(fgColor);
    }







    public void addPanel(DruPanel drup) {
        setColors(drup);
        dPanel.add(drup.getGUIPeer());
    }


    public void addMenu(DruMenu menu) {
        menu.setBg(bgColor);
        menu.setFg(fgColor);
        dPanel.add(menu.getGUIPeer());
    }


    public void addCardPanel(DruPanel drup) {
        setColors(drup);
        dPanel.add(drup.getGUIPeer(), drup.getTitle());
    }

    public void addPanel(DruPanel drup, Object constraints) {
        dPanel.add(drup.getGUIPeer(), constraints);
    }

    public void addRaw(JPanel jp, Object constraints) {
        dPanel.add(jp, constraints);
    }

    public void addDComponent(DComponent obj) {
        dPanel.addDComponent(obj);
    }

    public void addSingleDComponent(DComponent obj) {
        setSingle();
        dPanel.addDComponent(obj);
        if (tip != null && tip.length() > 3) {
            obj.setTooltip(tip);
        }
        tooltipTarget = obj;
    }

    public void addDComponent(DComponent cpt, Object constraints) {
        dPanel.addDComponent(cpt, constraints);
    }


    public void removeDComponent(DComponent cpt) {
        dPanel.removeDComponent(cpt);
    }


    public void removeAll() {
        dPanel.removeAll();
    }






    public void removePanel(DruPanel dp) {
        dPanel.remove(dp.getGUIPeer());
    }


    public void postApply() {
        // subclass for things taht use tips etc;
    }

    public void setSingle() {
        dPanel.setSingle();
    }



    public void revalidate() {
        dPanel.revalidate();
    }

    public void validate() {
        dPanel.validate();
    }

    public void repaint() {
        dPanel.repaint();
    }


    public void addBorder(int i, int j, int k, int l) {
        dPanel.addBorder(i, j, k, l);
    }

    public void setEmptyBorder(int i, int j, int k, int l) {
        dPanel.setEmptyBorder(i, j, k, l);
    }


    public void setPreferredSize(int prefWidth, int prefHeight) {
        dPanel.setPreferredSize(prefWidth, prefHeight);
    }


    public void addEtchedBorder(Color bg) {
        dPanel.addEtchedBorder(bg);
    }


    public void addTitledBorder(String borderTitle, Color c) {
        dPanel.addTitledBorder(borderTitle, c);
    }


    public void setEtchedBorder(Color bg) {
        dPanel.setEtchedBorder(bg);
    }

    public void setSunkenBorder(Color bg) {
        dPanel.setSunkenBorder(bg);
    }


    public void addSunkenBorder(Color bg) {
        dPanel.addSunkenBorder(bg);
    }

    public void setBorderLayout(int xspace, int yspace) {
        dPanel.setBorderLayout(xspace, yspace);
    }


    public Dimension getPreferredSize() {
        return dPanel.getPreferredSize();
    }


    public void setFlowLayout() {
        dPanel.setFlowLayout();
    }



    public void setGridLayout(int nr, int nc, int dx, int dy) {
        dPanel.setGridLayout(nr, nc, dx, dy);
    }

    public void setFlowLeft(int dx, int dy) {
        dPanel.setFlowLeft(dx, dy);
    }

    public void setFlowRight(int dx, int dy) {
        dPanel.setFlowRight(dx, dy);
    }

    public void setFlowCenter(int dx, int dy) {
        dPanel.setFlowCenter(dx, dy);
    }


    public void setEtchedUpBorder(Color c) {
        dPanel.setEtchedUpBorder(c);
    }


    public void setPreferredSize(Dimension d) {
        dPanel.setPreferredSize(d);
    }


    public int[] getXYLocationOnScreen() {
        return dPanel.getXYLocationOnScreen();
    }

    public void setBackgroundColor(Color color) {
        bgColor = color;
        setBg(bgColor);
    }

    public void setFallbackBackgroundColor(Color bg) {
        if (bgColor == null) {
            bgColor = bg;
            setBg(bg);
        }
    }

    public void seForegroundColor(Color color) {
        fgColor = color;
        setFg(fgColor);
    }

    public void setFallbackForegroundColor(Color fg) {
        if (fgColor == null) {
            fgColor = fg;
            setFg(fg);
        }
    }

}
