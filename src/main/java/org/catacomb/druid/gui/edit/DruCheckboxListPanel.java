package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.gui.base.DruListCellRenderer;
import org.catacomb.druid.swing.DCheckboxList;
import org.catacomb.druid.util.ListDisplay;
import org.catacomb.interlish.structure.ListWatcher;
import org.catacomb.report.E;


import java.awt.Color;
import java.util.ArrayList;

public class DruCheckboxListPanel extends DruGCPanel implements ListDisplay, LabelActor {
    static final long serialVersionUID = 1001;

    int nrow;

    DCheckboxList dList;

    ListWatcher listWatcher;

    String toggleAction;

    boolean multiple;


    public DruCheckboxListPanel() {
        this(10);
    }

    public DruCheckboxListPanel(int nr) {
        super();
        nrow = nr;

        dList = new DCheckboxList();

        addSingleDComponent(dList);


        dList.setItems(new Object[0]);

        dList.setLabelActor(this);
    }



    public void setItems(String[] sa) {
        dList.setItems(sa);
        if (listWatcher != null) {
            listWatcher.listChanged(this);
        }
    }


    public void setItems(ArrayList<? extends Object> obal) {
        Object[] obar = obal.toArray();
        dList.setItems(obar);

        if (listWatcher != null) {
            listWatcher.listChanged(this);
        }
    }


    public void setSelected(String[] sa) {
        dList.setSelected(sa);
    }

    public void setSelected(int[] ia) {
        dList.setSelected(ia);
    }

    public void selectAll() {
        dList.selectAll();
    }


    public ArrayList<Object> getAllItems() {
        return dList.getAllItems();
    }

    public ArrayList<Object> getSelectedItems() {
        return dList.getCheckedItems();
    }


    public void setBg(Color c) {
        dList.setBackground(c);
        super.setBg(c);
    }



    public void updateDisplay() {
        // EFF
        dList.repaint();
    }


    public Object getSelectedItem() {
        return dList.getSelectedValue();
    }


    public String getSelectedName() {
        return "" + getSelectedItem();
    }


    public void labelAction(String s, boolean b) {
        if (s.equals("selected")) {
            valueChange(getSelectedName());

        } else if (s.equals("toggle")) {

            if (toggleAction != null) {
                performAction(toggleAction, b);
                // valueChange(getSelectedName());
            }


            if (listWatcher != null) {
                listWatcher.listChanged(this);
            }

        } else {
            E.warning("unhandled " + s);
        }
    }


    public void setToggleAction(String ta) {
        toggleAction = ta;

    }


    public void clear() {
        dList.setListData(new String[0]);
    }


    public void selectAt(int i) {
        dList.selectAt(i);
    }

    public ArrayList<Object> getCheckedItems() {
        return dList.getCheckedItems();
    }



    public int[] getCheckedIndexes() {
        return dList.getCheckedIndexes();
    }


    public void setCheckedIndexes(int[] inds) {
        dList.setCheckedIndexes(inds);
    }



    public Object getLastSelected() {
        return dList.getLastSelected();
    }

    public boolean hasActiveSelected() {
        return dList.hasActiveSelected();
    }

    public void setListWatcher(ListWatcher lw) {
        listWatcher = lw;

    }

    public int[] getSelectedIndexes() {
        return dList.getCheckedIndexes();
    }



    public void setCellRenderer(DruListCellRenderer obj) {
        E.error("cannot set renderer for a checkbox list...");
        //  dList.setCellRenderer(obj.getGUIPeer());
    }

    public void setMultiple() {
        multiple = true;
        dList.setMultiple();
    }


    /*

    public Dimension getPreferredScrollableViewportSize() {
       return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
       return 10;
    }

    public boolean getScrollableTracksViewportHeight() {
       return false;
    }

    public boolean getScrollableTracksViewportWidth() {
       return true;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
       return 10;
    }
    */


}
