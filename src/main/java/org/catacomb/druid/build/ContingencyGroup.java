package org.catacomb.druid.build;


import java.util.ArrayList;


public class ContingencyGroup {

    String action;

    ArrayList<GroupContingent> contingents;

    public ContingencyGroup(String s) {
        contingents = new ArrayList<GroupContingent>();
    }

    public void setAction(String s) {
        action = s;
    }

    public boolean hasAction() {
        return (action != null);
    }

    public String getAction() {
        return action;
    }

    public void deselectOthers(GroupContingent ctg) {

        for (GroupContingent gc : contingents) {
            if (gc != ctg) {
                gc.deselect();
            }
        }
    }


    public void add(GroupContingent ctg) {
        contingents.add(ctg);
    }


}
