package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.Named;
import org.catacomb.report.E;


import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class DCheckboxList extends JList implements DComponent, ListSelectionListener,
    MouseListener {
    static final long serialVersionUID = 1001;



    static Font boldfont;

    public LabelActor labelActor;


    Object bufSel;

    boolean dropEvents;

    int hotspot = new JCheckBox().getPreferredSize().width;

    ToggleItem[] toggleItems;
    HashMap<String, ToggleItem> tiHM;

    ToggleItem activeTI;

    boolean multiple;


    public DCheckboxList() {
        super();
        dropEvents = false;
        addListSelectionListener(this);

        setCellRenderer(new CheckListCellRenderer(getCellRenderer()));
        addMouseListener(this);

        tiHM = new HashMap<String, ToggleItem>();
    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }


    public void setBg(Color c) {
        setBackground(c);
    }

    public void setMultiple() {
        multiple = true;
        super.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }





    public void setLabelActor(LabelActor la) {
        labelActor = la;
    }


    public void valueChanged(ListSelectionEvent lse) {
        if (dropEvents) {

        } else {
            if (lse.getValueIsAdjusting()) {

            } else {
                Object obj = getSelectedValue();
                activeTI = (ToggleItem)obj;

                //  ((ToggleItem)obj).toggle();

                if (obj != null && obj != bufSel && labelActor != null) {
                    bufSel = obj;
                    labelActor.labelAction("selected", true);

                }
            }
        }

        repaint();

        // repaint(getCellBounds(lse.getFirstIndex(), lse.getLastIndex()));

    }


    public void selectAt(int i) {
        dropEvents = true;
        setSelectedIndex(i);
        dropEvents = false;
    }



    private void toggleSelection(int index) {

        if (index >= 0) {
            toggleItems[index].toggle();
            activeTI = toggleItems[index];
            repaint();

            if (labelActor != null) {
                labelActor.labelAction("toggle", toggleItems[index].isOn());
            }


        }

        //repaint(getCellBounds(index, index+1));
    }


    public void mouseClicked(MouseEvent me) {

        int x = me.getX();

        int index = locationToIndex(me.getPoint());

        if (x <= 16 && index >= 0 && index < toggleItems.length) {
            toggleSelection(index);
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



    public void setItems(String[] sa) {
        Object[] oa = new Object[sa.length];
        for (int i = 0; i < sa.length; i++) {
            oa[i] = sa[i];
        }
        setItems(oa);
    }



    private String getLabel(Object obj) {
        String ret = null;
        if (obj instanceof Named) {
            ret = ((Named)obj).getName();
        } else {
            ret = obj.toString();
        }
        return ret;
    }


    public void setItems(Object[] oa) {
        ToggleItem[] ati = new ToggleItem[oa.length];
        for (int i = 0; i < oa.length; i++) {
            String key = getLabel(oa[i]);
            if (tiHM.containsKey(key)) {
                ati[i] = tiHM.get(key);
            } else {
                ati[i] = new ToggleItem(oa[i]);
            }
        }
        toggleItems = ati;

        tiHM.clear();
        for (ToggleItem ti : ati) {
            tiHM.put(ti.toString(), ti);
        }

        setListData(ati);

    }


    public ArrayList<Object> getCheckedItems() {
        ArrayList<Object> ret = new ArrayList<Object>();
        for (int i = 0; i < toggleItems.length; i++) {
            if (toggleItems[i].isOn()) {
                ret.add(toggleItems[i].getRef());
            }
        }
        return ret;
    }

    public ArrayList<Object> getAllItems() {
        ArrayList<Object> ret = new ArrayList<Object>();
        for (int i = 0; i < toggleItems.length; i++) {
            ret.add(toggleItems[i].getRef());
        }
        return ret;
    }


    public int[] getCheckedIndexes() {
        int[] wk = new int[toggleItems.length];
        int non = 0;
        for (int i = 0; i < toggleItems.length; i++) {
            if (toggleItems[i].isOn()) {
                wk[non++] = i;
            }
        }
        int[] ret = new int[non];
        for (int i = 0; i < non; i++) {
            ret[i] = wk[i];
        }
        return ret;
    }


    public void setCheckedIndexes(int[] inds) {
        for (int i = 0; i < toggleItems.length; i++) {
            toggleItems[i].setOff();
        }
        for (int i = 0; i < inds.length; i++) {
            toggleItems[inds[i]].setOn();
        }
        repaint();
    }



    public Object getLastSelected() {
        Object ret = null;
        if (activeTI != null) {
            ret = activeTI.getRef();
        }
        return ret;
    }

    public boolean hasActiveSelected() {
        boolean ret = false;
        if (activeTI != null) {
            ret = activeTI.isOn();
        }
        return ret;
    }


    public void selectAll() {
        dropEvents = true;
        for (ToggleItem ti : toggleItems) {
            ti.setOn();
        }
        dropEvents = false;
        repaint();
    }



    public void setSelected(int[] ia) {
        dropEvents = true;
        for (ToggleItem ti : toggleItems) {
            ti.setOff();
        }
        if (ia != null) {
            for (int i : ia) {
                if (i >= 0 && i < toggleItems.length) {
                    toggleItems[i].setOn();
                }
            }
        }

        dropEvents = false;
        repaint();
    }


    public void setSelected(String[] sa) {
        dropEvents = true;
        for (ToggleItem ti : toggleItems) {
            ti.setOff();
        }
        if (sa != null) {
            for (String s : sa) {
                if (tiHM.containsKey(s)) {
                    tiHM.get(s).setOn();
                } else {
                    E.warning("Checkbox List - trying to select an item that is not in the list: " + s);
                }
            }
        }
        dropEvents = false;
        repaint();
    }


    public void setToggleAction() {
        E.missing();

    }

}




