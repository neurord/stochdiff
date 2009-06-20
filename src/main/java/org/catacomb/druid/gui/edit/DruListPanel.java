package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.gui.base.DruListCellRenderer;
import org.catacomb.druid.gui.base.DruListClickActor;
import org.catacomb.druid.swing.DList;
import org.catacomb.druid.util.ListDisplay;
import org.catacomb.interlish.content.KeyedList;
import org.catacomb.interlish.interact.ClickListener;
import org.catacomb.interlish.structure.IDd;
import org.catacomb.interlish.structure.List;
import org.catacomb.interlish.structure.ListWatcher;
import org.catacomb.report.E;
import org.catacomb.util.ArrayUtil;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;


public class DruListPanel extends DruGCPanel
    implements ListDisplay, LabelActor, ClickListener, ListWatcher, List {

    static final long serialVersionUID = 1001;

    public static final int NORMAL_ORDER = 100;
    public static final int REVERSE_ORDER = 101;
    int order = NORMAL_ORDER;


    int nrow;

    DList dList;

    Object[] items;
    String[] tooltips;

    ArrayList<DruListClickActor> clickActors;

    KeyedList<? extends IDd> targetKL;

    boolean multiple = false;


    public DruListPanel() {
        this(10);
    }


    public DruListPanel(int nr) {
        super();
        nrow = nr;

        dList = new DList();
        addSingleDComponent(dList);

        Object[] sa = new String[0];
        dList.setListData(sa);

        dList.setLabelActor(this);
    }



    public void setItems(Object[] sain) {
        Object[] sa = sain;
        if (sa == null) {
            sa = new String[0];
        }
        items = sa;
        dList.setListData(sa);
        dList.clearBufferedSelection();
    }


    public void setItems(ArrayList<? extends Object> obal) {
        HashSet<Object> oldsels = new HashSet<Object>();
        for (int i : getSelectedIndexes()) {
            oldsels.add(items[i]);
        }

        Object[] obar = new Object[obal.size()];
        String[] ott = new String[obal.size()];

        int iel = 0;
        for (Object elt : obal) {
            if (elt instanceof String[]) {
                String[] sp = (String[])elt;
                obar[iel] = sp[0];
                if (sp.length > 1) {
                    ott[iel] = sp[1]; // tool tip;
                }

            } else {
                obar[iel] = elt;
            }
            iel += 1;
        }

        items = obar;
        tooltips = ott;
        dList.setListData(obar);
        dList.setTooltips(tooltips);
        /*
         * E.missing("dlist needs the raw items for the renderer");
         *
         * if (oba != null) { String[] sa = new String[oba.size()]; int ic = 0;
         * for (Object ob : oba) { sa[ic++] = ob.toString(); }
         * dList.setListData(sa); }
         */


        for (int i = 0; i < items.length; i++) {
            if (oldsels.contains(items[i])) {
                dList.selectAt(i);
            }
        }
    }

    public void setMultiple() {
        multiple = true;
        dList.setMultiple();
    }

    public void setBg(Color c) {
        setEtchedBorder(c);
        // dList.setBackground(c);
        super.setBg(c);
    }


    public void setCellRenderer(DruListCellRenderer obj) {
        dList.setCellRenderer(obj.getGUIPeer());
    }



    public void updateDisplay() {
        // EFF
        dList.repaint();
    }


    public Object getSelectedItem() {
        return dList.getSelectedValue();
    }

    public int getSelectedIndex() {
        return dList.getSelectedIndex();
    }

    public String getSelectedName() {
        return "" + getSelectedItem();
    }


    public void labelAction(String s, boolean b) {
        if (s.equals("selected")) {
            String ssel = getSelectedName();

            //       E.info("list reporting vc ");

            valueChange(ssel);
        }
    }


    public void clear() {
        dList.setListData(new String[0]);
    }


    public void selectAt(int i) {
        dList.selectAt(i);
    }

    public void setSelectedItem(Object obj) {
        setSelected(obj);
    }

    public void setSelected(Object obj) {
        // NB normally the items are the string ids, not hte things themselves if ther is a kl
        /*
        if (obj instanceof String && targetKL != null) {
           if (targetKL.hasItem((String)obj)) {

              obj = targetKL.get((String)obj);
           }
        }
        */


        int isel = -1;
        if (obj != null) {
            for (int i = 0; i < items.length; i++) {
                if (obj.equals(items[i])) {
                    isel = i;
                    break;
                }
            }
            if (isel < 0) {
                E.warning(" not in list " + obj);
            }
        }

        dList.selectAt(isel);
    }


    public void addClickAction(DruListClickActor actor) {
        if (clickActors == null) {
            clickActors = new ArrayList<DruListClickActor>();
            dList.setClickListener(this);
        }
        clickActors.add(actor);
    }


    public void pointClicked(int x, int y, int b) {
        if (clickActors != null) {
            for (DruListClickActor ca : clickActors) {
                ca.clicked(x, this);
            }
        }

    }


    public void setKeyedList(KeyedList<? extends IDd> idkl) {
        if (targetKL != null) {
            targetKL.removeListWatcher(this);
        }
        targetKL = idkl;
        targetKL.addListWatcher(this);
        syncFromKeyedList();
    }


    private void syncFromKeyedList() {
        Object osel = getSelectedItem();


        ArrayList<String> als = targetKL.getKeys();
        String[] sa = als.toArray(new String[0]);

        if (order == REVERSE_ORDER) {
            sa = ArrayUtil.reverseStringArray(sa);
        } else {
        }
        setItems(sa);

        if (osel != null) {
            setSelectedItem(osel);
        } else {
            setSelectedItem(null);
        }
    }


    public void listChanged(Object src) {
        if (targetKL != null) {
            syncFromKeyedList();
        }
    }


    public void addLine() {
        dList.addLine();
    }


    public void removeLine() {
        dList.removeLine();
    }


    public void ensureHasSelection() {
        dList.ensureHasSelection();
    }


    public boolean isSelectionEmpty() {
        return dList.isSelectionEmpty();
    }


    public int listSize() {
        return dList.listSize();
    }



    public void able(boolean b) {
        dList.setEnabled(b);
    }


    public void setOrder(int ord) {
        order = ord;
    }


    public int[] getSelectedIndexes() {
        return dList.getSelectedIndices();
    }



    /*
     *
     * public Dimension getPreferredScrollableViewportSize() { return
     * getPreferredSize(); }
     *
     * public int getScrollableBlockIncrement(Rectangle visibleRect, int
     * orientation, int direction) { return 10; }
     *
     * public boolean getScrollableTracksViewportHeight() { return false; }
     *
     * public boolean getScrollableTracksViewportWidth() { return true; }
     *
     * public int getScrollableUnitIncrement(Rectangle visibleRect, int
     * orientation, int direction) { return 10; }
     */


}
