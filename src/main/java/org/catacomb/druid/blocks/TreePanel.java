package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruChoice;
import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.druid.gui.edit.DruTreePanel;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;



public class TreePanel extends Panel implements AddableTo {


    public String pivotSelection;

    public String flavor;
    public String show;

    public String action;
    public PopupMenu popup;

    public boolean showRoot;
    public boolean draggable;



    public TreePanel() {
        super();
        showRoot = true;

    }


    public void add(Object obj) {
        if (obj instanceof PopupMenu) {
            popup = (PopupMenu)obj;
        } else {
            E.error("cant add " + obj);
        }
    }


    public DruPanel instantiatePanel() {
        DruTreePanel drup = new DruTreePanel();
        return drup;
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {
        DruTreePanel drup = (DruTreePanel)dp;
        drup.setID(id);


        drup.setRootVisibility(showRoot);


        if (action != null) {
            drup.setAction(action);
        }

        if (show != null) {
            ctx.getMarketplace().addConsumer("Tree", drup, show);
        }

        if (pivotSelection != null) {
            if (pivotSelection.equals("none")) {


            } else {
                String[] sa = { "order 1", "order 2", "order 3" };

                DruChoice pivotChoice = new DruChoice(sa, "repivot");
                pivotChoice.setTooltip("alternative views of the tree");

                drup.setPivotChoice(pivotChoice);
            }
        }

        if (popup != null) {
            DruMenu drum = (DruMenu)(popup.realize(ctx, gpath));
            drup.setMenu(drum);
        }

        ctx.getMarketplace().addVisible("TreeSelection", drup, flavor); // was "selection" not flavor;

        if (draggable) {
            drup.enableDrag();
        }


    }

}
