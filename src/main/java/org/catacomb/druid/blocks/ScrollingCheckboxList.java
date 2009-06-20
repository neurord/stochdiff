
package org.catacomb.druid.blocks;

import java.util.ArrayList;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruListColorRenderer;
import org.catacomb.druid.gui.base.DruListProgressRenderer;
import org.catacomb.druid.gui.base.DruListQuantityRenderer;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruListPanel;
import org.catacomb.druid.gui.edit.DruScrollingCheckboxListPanel;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;

public class ScrollingCheckboxList extends Panel implements AddableTo {


    public String action;
    public String toggleAction;
    public int nrow;

    public String renderer;

    public String order;
    public boolean multiple;

    public String clickAction;

    ArrayList<ListClickArea> clickAreas;



    public void add(Object obj) {
        if (obj instanceof ListClickArea) {
            if (clickAreas == null) {
                clickAreas = new ArrayList<ListClickArea>();
            }
            clickAreas.add((ListClickArea)obj);
        }
    }


    public DruPanel instantiatePanel() {
        if (nrow == 0) {
            nrow = 10;
        }
        return new DruScrollingCheckboxListPanel(nrow);
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruScrollingCheckboxListPanel drup = (DruScrollingCheckboxListPanel)dp;
        drup.setAction(action);
        if (toggleAction != null) {
            drup.setToggleAction(toggleAction);
        }

        if (renderer != null) {

            // TODO move renderer defs to XML;

            if (renderer.equals("quantity")) {
                drup.setCellRenderer(new DruListQuantityRenderer());

            } else if (renderer.equals("progress")) {
                drup.setCellRenderer(new DruListProgressRenderer());

            } else if (renderer.equals("color")) {
                drup.setCellRenderer(new DruListColorRenderer());

            } else {
                E.error("unrecognized renderer " + renderer);
            }
        }

        if (multiple) {
            drup.setMultiple();
        }

        if (clickAction != null) {
            if (clickAction.equals("toggle")) {
                drup.setToggleAction();
            } else {
                E.warning("unrecognized action " + clickAction);
            }
        }


        if (order != null) {
            if (order.equals("reverse")) {
                drup.setOrder(DruListPanel.REVERSE_ORDER);

            } else {
                E.warning("unknown list order " + order + " (only know reverse)");
            }
        }


        if (clickAreas != null) {
            for (ListClickArea lca : clickAreas) {
                drup.addClickAction(lca.makeActor());
            }
        }

    }

}


