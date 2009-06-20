package org.catacomb.interlish.content;

import org.catacomb.be.ChangeEvent;
import org.catacomb.interlish.content.BasicTouchTime;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.util.ArrayList;

public class PrimitiveValue implements Value, TouchTimed, ParentReporter {


    private BasicTouchTime touchTime;

    private ArrayList<ValueWatcher> watchers;
    private ArrayList<UseWatcher> useWatchers;

    private ChildListener childListener; // single upstream listener;
    String reportableID;

    private boolean able;

    private boolean reportingUse;


    public PrimitiveValue() {
        touchTime = new BasicTouchTime();
        able = true;
    }

    public PrimitiveValue(boolean b) {
        touchTime = new BasicTouchTime();
        able = b;
    }


    public void setAble(boolean b) {
        if (b != able) {
            able = b;
            reportValueChange(this);
        }
    }

    public boolean isAble() {
        return able;
    }

    public void setChildListener(ChildListener cl, String srcid) {
        childListener = cl;
        reportableID = srcid;
    }


    public void removeChildListener() {
        childListener = null;
    }


    public void addValueWatcher(ValueWatcher vw) {
        if (watchers == null) {
            watchers = new ArrayList<ValueWatcher>();
        }
        watchers.add(vw);
    }

    public void removeValueWatcher(ValueWatcher vw) {
        if (watchers == null) {
            E.warning("cant remove - not present");
        } else {
            if (watchers.contains(vw)) {
                watchers.remove(vw);

            } else {
                E.warning("attempted to remove a watcher that isnt in the list");
            }
        }
    }



    public void addUseWatcher(UseWatcher vw) {
        if (useWatchers == null) {
            useWatchers = new ArrayList<UseWatcher>();
        }
        useWatchers.add(vw);
    }

    public void removeUseWatcher(UseWatcher vw) {
        if (useWatchers == null) {
            E.warning("cant remove - not present");
        } else {
            if (useWatchers.contains(vw)) {
                useWatchers.remove(vw);

            } else {
                E.warning("attempted to remove a watcher that isnt in the list");
            }
        }
    }





    public BasicTouchTime getTouchTime() {
        return touchTime;
    }


    public void reportValueChange(Object src) {
        if (watchers != null) {
            for (ValueWatcher w : watchers) {
                w.valueChangedBy(this, src);
            }
        }
        valueChanged();
    }


    public void reportUse(Object src) {
        if (!reportingUse) {
            reportingUse = true;
            if (useWatchers != null) {
                for (UseWatcher w : useWatchers) {
                    w.usedBy(this, src);
                }
            }
            reportingUse = false;
        }
    }


    public void logChange() {
        touchTime.now();
    }

    public boolean changedSince(TouchTimed tt) {
        return (touchTime.isAfter(tt.getTouchTime()));
    }

    // typically called when an editor loses focus;
    // TODO check called as well as value change at end of sequence
    public void editCompleted() {
        touchTime.now();
        // already sent valuechanged from above?
        //      valueChanged();
    }

    public void valueChanged() {
        if (childListener != null) {
            childListener.childChanged(new ChangeEvent(reportableID, this));
        }
    }

}
