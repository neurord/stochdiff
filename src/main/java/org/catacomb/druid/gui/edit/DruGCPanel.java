package org.catacomb.druid.gui.edit;

import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.ActionRelay;
import org.catacomb.interlish.structure.ActionSource;




class DruGCPanel extends DruPanel implements ActionSource {
    static final long serialVersionUID = 1001;


    String methodName;
    ActionRelay actionRelay;

    // boolean relaying;



    public DruGCPanel() {
        super();
        //   relaying = true;
        methodName = null;
    }





    public void setAction(String s) {
        setActionMethod(s);
    }

    public void setActionMethod(String s) {
        methodName = s;
    }

    public void setMethodName(String s) {
        setActionMethod(s);
    }

    public String getAction() {
        return methodName;
    }

    public boolean hasAction() {
        return (methodName != null);
    }

    public void disableRelaying() {
        //   relaying = false;
    }


    public boolean hasRelay() {
        return (actionRelay != null);
    }


    public void setActionRelay(ActionRelay ar) {
        actionRelay = ar;
    }



    public ActionRelay getActionRelay() {
        return actionRelay;
    }




    public void valueChange(String s) {
        action(s);
    }


    public void valueChange(boolean b) {

        action(b);
    }

    public void unstoredValueChange(boolean b) {
        action(b);
    }

    public void valueChange(double d) {
        action(d);
    }

    public void valueChange(int i) {
        action(i);
    }

    public void valueChange(Object obj) {
        action(obj);
    }



    public void performAction(String mnm) {
        if (actionRelay != null) {
            actionRelay.action(mnm);
        }
    }
    public void performAction(String mnm, boolean b) {
        if (actionRelay != null) {
            actionRelay.actionB(mnm, b);
        }
    }


    public void action() {
        if (methodName != null && actionRelay != null) {
            actionRelay.action(methodName);
        }
    }


    public void action(boolean b) {
        if (methodName != null && actionRelay != null) {
            actionRelay.actionB(methodName, b);
        }
    }


    public void action(String sarg) {
        if (actionRelay != null && methodName != null) {
            actionRelay.actionS(methodName, sarg);
        }
    }


    public void action(double d) {
        if (methodName != null && actionRelay != null) {
            actionRelay.actionD(methodName, d);
        }
    }

    public void action(int i) {
        if (methodName != null && actionRelay != null) {
            actionRelay.actionI(methodName, i);
        }
    }


    public void action(Object obj) {
        if (methodName != null && actionRelay != null) {
            actionRelay.actionO(methodName, obj);
        }
    }

    public void action(String mnm, String arg) {
        if (actionRelay != null) {
            actionRelay.actionS(mnm, arg);
        }
    }

}
