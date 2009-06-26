package org.catacomb.druid.gui.edit;

import org.catacomb.interlish.structure.TargetStore;
import org.catacomb.interlish.structure.TargetStoreUser;
import org.catacomb.interlish.structure.Viewer;
import org.catacomb.report.E;



public class Effect implements Viewer, TargetStoreUser {

    String targetID;

    Object target;

    TargetStore targetStore;


    public Effect() {
        this(null);
    }


    public Effect(String s) {
        targetID = s;
    }


    public void setTargetStore(TargetStore ts) {
        targetStore = ts;
    }


    public String getTargetID() {
        return targetID;
    }

    public void apply(boolean b) {
        E.override();
    }

    public void apply(String s) {
        E.override();
    }


    public Object getTarget() {
        if (targetID == null || targetID.equals("null")) {

        } else {
            if (target == null) {
                if (targetStore == null) {
                    E.error("no target store in effect " + this);
                } else {
                    if (targetID.indexOf(",") > 0) {
                        String[] sa = targetID.split(",");
                        Object[] ret = new Object[sa.length];
                        int iret = 0;
                        for (String s : sa) {
                            ret[iret++] = targetStore.get(s.trim());
                        }
                        target = ret;


                    } else {
                        target = targetStore.get(targetID);
                    }
                }

            }
        }
        return target;
    }



}
