package org.catacomb.graph.gui;

import org.catacomb.interlish.content.KeyedList;
import org.catacomb.interlish.structure.IDable;
import org.catacomb.interlish.structure.IDd;
import org.catacomb.report.E;


import java.util.ArrayList;

// REFAC - redundant - just use keyed list?
public class ViewSet implements IDable {

    String id;

    KeyedList<DataView> views;

    String latestNew = null;

    public ViewSet() {
        views = new KeyedList<DataView>();
    }

    public ViewSet(String s) {
        this();
        id = s;
    }

    public KeyedList<? extends IDd> getDataViews() {
        return views;
    }

    public void setID(String s) {
        id = s;
    }

    public String getID() {
        return id;
    }


    public void addView(DataView dv) {
        if (views.hasItem(dv.getID())) {
            views.remove(dv.getID());
        }
        views.add(dv);
    }



    public String newViewName() {
        return views.newName("view");
    }

    public DataView getDataView(String s) {
        return views.get(s);
    }

    public boolean hasView(String s) {
        return views.hasItem(s);
    }


    public void addIfNew(ArrayList<DataView> newViews) {
        boolean added = false;
        latestNew = null;
        for (DataView dv : newViews) {
            String sid = dv.getID();
            if (sid == null) {
                E.error("view with no id " + dv);
            } else if (hasView(dv.getID())) {
                // do nothing;
            } else {
                //  E.info("adding a new view to " + views.size() + " " + dv.getID());
                views.silentAddItem(dv);
                added = true;
                latestNew = dv.getID();
            }
        }
        if (added) {
            views.reportChange();
        }
    }


    public String latestAddition() {
        return latestNew;
    }


    public void printViews() {
        E.info("all views: " + views.printIDs());
    }

}
