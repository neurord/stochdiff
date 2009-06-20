package org.catacomb.druid.gui.base;


import org.catacomb.druid.event.StateListener;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.report.E;


public class DruPseudoCardPanel extends DruSubcontainerPanel {

    static final long serialVersionUID = 1001;

    int npanel;
    DruPanel[] panels;
    String[] names;

    int ipanel;

    int nlistener;
    StateListener[] listeners;


    public DruPseudoCardPanel() {
        panels = new DruPanel[10];
        names = new String[10];

        setSingle();

        panels = new DruPanel[10];
        names = new String[10];
    }



    public void subAddPanel(DruPanel drup) {
        setColors(drup);

        if (bgColor.getRed() == 240 && bgColor.getGreen() == 240) {
            E.info("got the 240 color...");
            // (new Exception()).printStackTrace();
        }


        panels[npanel] = drup;
        names[npanel] = drup.getTitle();

        npanel += 1;

        if (npanel == 1) {
            getGUIPeer().addDComponent(drup.getGUIPeer());
            ipanel = 0;
        }
    }


    public void subAddDComponent(DComponent dcpt) {
        E.error("only accept DruPanels");
    }



    public boolean containsCard(String s) {
        return (getCardIndex(s) >= 0);
    }


    private int getCardIndex(String s) {
        int ishow = -1;
        for (int i = 0; i < npanel; i++) {
            if (s.equals(names[i])) {
                ishow = i;
            }
        }
        return ishow;
    }


    public void showCard(String s) {
        int ishow = getCardIndex(s);

        if (ishow == -1) {
            E.error(" - unknown card in pseudo card panel " + s);

        } else if (ishow == ipanel) {
            // nothing to do;

        } else {
            showIthCard(ishow);

        }

        revalidate();

    }



    private void notifyListeners(String stat) {
        for (int i = 0; i < nlistener; i++) {
            listeners[i].stateChanged(stat);
        }
    }



    public void addStateListener(StateListener sl) {
        if (listeners == null) {
            listeners = new StateListener[4];
        }
        listeners[nlistener++] = sl;
    }



    public void nextCard() {

        ipanel = (ipanel + 1) % npanel;
        if (ipanel == npanel) {
            ipanel = 0;
        }
        showIthCard(ipanel);
    }



    private void showIthCard(int ip) {
        getGUIPeer().removeDComponent(panels[ipanel].getGUIPeer());
        ipanel = ip;
        getGUIPeer().addDComponent(panels[ipanel].getGUIPeer());

        notifyListeners(names[ip]);
        revalidate();
    }


    public void subRemoveAll() {
        E.missing();
    }

}
