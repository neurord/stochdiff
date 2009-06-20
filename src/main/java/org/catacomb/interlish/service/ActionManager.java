package org.catacomb.interlish.service;

import java.util.HashMap;

import org.catacomb.report.E;



public class ActionManager {

    static ActionManager instance;

    private HashMap<String, Performer> performerHM;



    public static ActionManager get() {
        if (instance == null) {
            instance = new ActionManager();
        }
        return instance;
    }




    public ActionManager() {
        performerHM = new HashMap<String, Performer>();
    }

    public void addPerformer(String s, Performer p) {
        performerHM.put(s, p);
    }

    public void perform(String act, Object arg) {
        if (performerHM.containsKey(act)) {
            performerHM.get(act).performAction(act, arg);
        } else {
            E.error("no performer for action " + act + " " + arg);
        }
    }

}
