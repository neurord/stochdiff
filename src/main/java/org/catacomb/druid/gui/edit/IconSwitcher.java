
package org.catacomb.druid.gui.edit;


import org.catacomb.druid.event.StateListener;



// really don't want to duplicate all this data for every instance - lots of instances
// should have same set of states - need a two part object: exec and state ?

public class IconSwitcher implements StateListener {

    int nstate;
    String[] states;
    String[] srcs;


    DruButton target;

    public IconSwitcher(DruButton but) {
        target = but;
        nstate = 0;
        states = new String[4];
        srcs = new String[4];
    }


    public void addResponse(String stat, String src) {
        states[nstate] = stat;
        srcs[nstate] = src;
        nstate += 1;
    }


    public void stateChanged(String s) {
        for (int i = 0; i < nstate; i++) {
            if (states[i].equals(s)) {
                target.setIconSource(srcs[i]);
                break;
            }
        }
    }



}
