package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DMenu;
import org.catacomb.druid.swing.DMenuItem;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;
import org.catacomb.util.StringTree;
import org.catacomb.util.StringTreeLeaf;
import org.catacomb.util.StringUtil;


import java.awt.Color;
import java.util.ArrayList;



public class DruMenu implements PopulableMenu, ActionSource,
    LabelActor, Syncable {

    static final long serialVersionUID = 1001;

    String name;

    String id;

    DruMenu parentMenu;

    protected String methodName;

    ActionRelay actionRelay;

    ArrayList<Syncable> items;

    DMenu dMenu;


    public DruMenu(String s) {
        dMenu = new DMenu(s);
        name = s;

        items = new ArrayList<Syncable>();
        dMenu.applyRollover();
    }

    public DMenu getGUIPeer() {
        return dMenu;
    }


    void setParentMenu(DruMenu dm) {
        parentMenu = dm;
    }


    public String toString() {
        return "DruMenu " + name;
    }


    public String getID() {
        return id;
    }


    public void addSubMenu(DruMenu dsm) {
        items.add(dsm);
        dMenu.add(dsm.getGUIPeer());
        dsm.setParentMenu(this);
    }



    public void addItem(DruMenuItem dmi) {
        items.add(dmi);
        dMenu.add(dmi.getGUIPeer());

    }

    public void addItem(DruCheckboxMenuItem dmi) {
        items.add(dmi);
        dMenu.add(dmi.getGUIPeer());
    }

    public void addItem(String s) {
        DMenuItem dmi = new DMenuItem(s);
        dMenu.add(dmi);
        dmi.setLabelActor(this);

    }

    public void addItem(String lab, String val) {
        DMenuItem dmi = new DMenuItem(lab, val);
        dMenu.add(dmi);
        dmi.setLabelActor(this);

    }

    public void setBg(Color background) {
        dMenu.setBackground(background);
    }


    public void setFg(Color foreground) {
        dMenu.setForeground(foreground);
    }


    public void setAction(String s) {
        methodName = s;
    }


    public boolean hasRelay() {
        return (actionRelay != null);
    }


    public void setActionRelay(ActionRelay ac) {
        actionRelay = ac;
    }


    public void labelAction(String s, boolean b) {
        //  E.info("dmla " + name + " " + label + " " + id + " "+ s);

        if (actionRelay != null) {
            actionRelay.actionS(methodName, s);

        } else if (parentMenu != null) {
            parentMenu.labelAction(s, b);

        } else {
            E.warning("dropped menu action " + s);
        }
    }


    public void setOptions(String[] sa) {
        setItems(sa);
    }

    public void setItems(String[] sa) {
        dMenu.removeAll();
        if (sa == null || sa.length == 0) {
            dMenu.setEnabled(false);
        } else {
            dMenu.setEnabled(true);
            for (int i = 0; i < sa.length; i++) {
                addItem(sa[i]);
            }
        }
    }


    public void setItemsTree(String[] sa) {
        dMenu.removeAll();
        //  StringTree stree = StringUtil.treeify(sa);
        StringTree stree = StringUtil.flatTreeify(sa, "root");
        for (Object obj : stree.getChildren()) {
            if (obj instanceof String) {
                addItem((String)obj, (String)obj);

            } else if (obj instanceof StringTree) {
                addSubMenu(makeMenu((StringTree)obj));

            } else if (obj instanceof StringTreeLeaf) {
                String stln = ((StringTreeLeaf)obj).getLabel();
                addItem(stln, stln);

            } else {
                E.error("unkown item " + obj);
            }
        }
    }


    private DruMenu makeMenu(StringTree stree) {
        DruMenu dm = new DruMenu(stree.getLabel());
        for (Object obj : stree.getChildren()) {
            if (obj instanceof String) {
                dm.addItem((String)obj,   stree.getPath() + obj);

            } else if (obj instanceof StringTreeLeaf) {
                StringTreeLeaf stl = (StringTreeLeaf)obj;
                dm.addItem(stl.getLabel(), stree.getPath() + stl.getLabel());

            } else if (obj instanceof StringTree) {
                dm.addSubMenu(makeMenu((StringTree)obj));

            } else {
                E.error("unkown item " + obj);
            }
        }
        return dm;
    }


    public void addSeparator() {
        dMenu.addSeparator();
    }





    public void setID(String s) {
        id = s;
    }



    public void preShowSync() {
        sync();
    }


    public void sync() {
        for (Syncable sbl : items) {
            sbl.sync();
        }
    }

    public void showPopup(DruMenuButton button, int i, int j) {
        dMenu.showPopup(button.getGUIPeer(), i, j);
    }


}
