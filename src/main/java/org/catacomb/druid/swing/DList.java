package org.catacomb.druid.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.interlish.interact.ClickListener;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.util.MouseUtil;


public class DList extends JList implements DComponent, ListSelectionListener, MouseListener {

    static final long serialVersionUID = 1001;
    static Font plainfont;
    static Font boldfont;

    public LabelActor labelActor;
    Object bufSel;

    boolean dropEvents;
    ClickListener clickListener;
    String[] tooltips;
    int nline = 3;
    boolean multiple = false;

    HashSet<Object> siHS = new HashSet<Object>();


    public DList() {
        super();
        dropEvents = false;
        setPlainFont();
        addListSelectionListener(this);
    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }

    public void setBg(Color c) {
        setBackground(c);
    }


    public void clearBufferedSelection() {
        bufSel = null;
    }


    public void setMultiple() {
        multiple = true;
        super.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }



    public void setTooltips(String[] sa) {
        tooltips = sa;
    }


    public void setPlainFont() {
        if (plainfont == null) {
            plainfont = new Font("sansserif", Font.PLAIN, 12);
        }

        setFont(plainfont);
    }


    public String getToolTipText(MouseEvent me) {
        int idx = locationToIndex(me.getPoint());
        String ret = null;
        if (idx >= 0 && tooltips != null && tooltips.length > idx) {
            ret = tooltips[idx];
        }
        if (ret == null) {
            ret = "";
        }
        return ret;
    }



    public void setLabelActor(LabelActor la) {
        labelActor = la;
    }


    public void valueChanged(ListSelectionEvent lse) {
        if (dropEvents) {

        } else if (lse != null && lse.getValueIsAdjusting()) {

        } else {
            if (multiple) {
                Object onew = null;
                Object ofirst = null;
                for (Object obj : getSelectedValues()) {
                    if (siHS.contains(obj)) {
                        if (ofirst == null) {
                            ofirst = obj;
                        }
                    } else {
                        if (onew == null) {
                            onew = obj;
                        }
                    }
                }
                siHS.clear();
                for (Object obj : getSelectedValues()) {
                    siHS.add(obj);
                }
                if (onew != null) {
                    bufSel = onew;
                    labelActor.labelAction("selected", true);
                } else if (ofirst != null) {
                    bufSel = ofirst;
                    labelActor.labelAction("selected", true);
                } else {
                    bufSel = null;
                    labelActor.labelAction("selected", false);
                }


            } else {
                Object obj = getSelectedValue();
                //   E.info("sel " + obj + " " + bufSel + " " + (obj == bufSel));
                if (obj != null && obj != bufSel && labelActor != null) {
                    bufSel = obj;
                    labelActor.labelAction("selected", true);

                }
            }
        }

    }


    public void selectAt(int i) {
        dropEvents = true;
        if (multiple) {
            addSelectionInterval(i, i);

        } else {
            if (i >= 0) {
                setSelectedIndex(i);
            } else {
                bufSel = null;
                clearSelection();
            }
        }
        dropEvents = false;
    }


    public void setClickListener(ClickListener cl) {
        clickListener = cl;
        addMouseListener(this);
    }


    public void mouseClicked(MouseEvent e) {
        if (clickListener != null) {
            clickListener.pointClicked(e.getX(), e.getY(), MouseUtil.getButton(e));
        }

    }


    public void mousePressed(MouseEvent e) {

    }


    public void mouseReleased(MouseEvent e) {
    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }


    public void addLine() {
        nline += 1;
        setPreferredSize(new Dimension(100, 20 * nline));
    }


    public void removeLine() {
        if (nline > 3) {
            nline -= 1;
        }
        setPreferredSize(new Dimension(100, 20 * nline));
    }


    public void ensureHasSelection() {
        if (isSelectionEmpty() && getModel().getSize() > 0) {
            selectAt(0);
            valueChanged(null);
        }
    }

    public int listSize() {
        return getModel().getSize();
    }


    public void setToggleAction() {
        setSelectionModel(new ToggleSelectionModel());
    }


    /*
     *
     * public void setBoldFont() { if (boldfont == null) { boldfont = new Font
     * ("sansserif", Font.BOLD, 12); } setFont(boldfont); }
     */
}
