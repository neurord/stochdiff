
package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;



public class DPopLabel extends JPanel implements LabelActor {

    private static final String SHOW_MENU = "showMenu";

    static final long serialVersionUID = 1001;

    int type = 0;
    boolean fixedSize = false;

    public final static int SIMPLE = 0;
    public final static int CHECKBOX = 1;

    JPopupMenu mymenu;
    DButton popB;


    String latestChoice;

    LabelActor ml;

    Menued menued;
    String[] options;



    public DPopLabel(String s) {
        this(s, s);
    }

    public DPopLabel(String s, String itxt) {
        popB = new DButton(itxt);
        popB.setActionCommand(SHOW_MENU);

        mymenu = new JPopupMenu();
        add(mymenu);
        popB.setLabelActor(this);

        setLayout(new BorderLayout());
        add("Center", popB);
    }



    public void setBg(Color c) {
        popB.setBg(c);
        setBackground(c);
    }


    public void clear() {
        // popB.remove(mymenu);
        // mymenu = new JPopupMenu();
        // popB.add(mymenu);

        mymenu.removeAll();
    }


    public void setLabel(String s) {
        popB.setText(s);
    }


    public void setEnabled(boolean b) {
        popB.setEnabled(b);
        mymenu.setEnabled(b);
    }


    public void setLabelActor(LabelActor ml) {
        this.ml = ml;
    }


    public void setFixedSize(boolean b) {
        fixedSize = b;
    }


    public Dimension getPreferredSize() {
        if (fixedSize) {
            return new Dimension(60, 20);
        } else {
            return popB.getPreferredSize();
        }
    }


    public void setType(int ityp) {
        type = ityp;
    }


    public void setText(String sin) {
        String s = sin;
        if (s == null)
            s = "";
        if (!s.equals(popB.getText())) {
            popB.setText(s);
            popB.invalidate();
            validate();
        }
    }


    public String getText() {
        return popB.getText();
    }


    public String getLabel() {
        return popB.getText();
    }


    public void myShowMenu() {

        if (menued != null) {
            String[] sa = menued.getMenuOptions();
            if (sa != null && sa != options) {
                setMenuOptions(sa);
            }
        }

        checkNames();
        mymenu.show(this, 0, 25);
    }


    public void showMenu() {
        myShowMenu();
    }


    public void hideMenu() {
        mymenu.setVisible(false);
    }



    public void armSelection(String s) {
        Component[] cs = mymenu.getComponents();
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] instanceof DMenuItem) {
                DMenuItem cmi = (DMenuItem)cs[i];
                if (cmi.actionCommand.equals(s)) {
                    cmi.setArmed(true);
                } else {
                    cmi.setArmed(false);
                }
            }
        }

    }


    public void repaintMenu() {
        mymenu.repaint();
    }



    public void labelAction(String sarg, boolean b) {

        if (sarg.equals(SHOW_MENU)) {
            myShowMenu();

        } else {
            deliverLabelAction(sarg, b);
        }
    }



    public void deliverLabelAction(String sarg, boolean b) {
        latestChoice = sarg;
        if (ml != null)
            ml.labelAction(sarg, b);
    }




    public void checkNames() {


        MenuElement[] jme = mymenu.getSubElements();
        for (int i = 0; i < jme.length; i++) {
            Object ob = jme[i];
            if (ob instanceof DMenuItem) {
                ((DMenuItem)ob).checkName();
            }
        }
    }


    public String[] getNames() {
        MenuElement[] jme = mymenu.getSubElements();
        String[] sa = new String[jme.length];
        for (int i = 0; i < jme.length; i++) {
            Object ob = jme[i];
            if (ob instanceof DMenuItem) {
                sa[i] = ((DMenuItem)(mymenu.getComponent(i))).getText();
            }
        }
        return sa;
    }


    public boolean[] getStates() {
        MenuElement[] jme = mymenu.getSubElements();
        boolean[] ba = new boolean[jme.length];
        for (int i = 0; i < jme.length; i++) {
            Object mi = jme[i];
            ba[i] = false;
            if (mi instanceof DCheckboxMenuItem) {
                DCheckboxMenuItem tmi = (DCheckboxMenuItem)mi;
                ba[i] = tmi.getState();
            }
        }
        return ba;
    }


    public void setStates(String[] onm, boolean ob[]) {
        MenuElement[] jme = mymenu.getSubElements();
        for (int i = 0; i < jme.length; i++) {
            Object mi = jme[i];
            if (mi instanceof DCheckboxMenuItem) {
                DCheckboxMenuItem tmi = (DCheckboxMenuItem)mi;
                String s = tmi.getText();
                for (int k = 0; k < onm.length; k++) {
                    if (s.equals(onm[k])) {
                        tmi.setState(ob[k]);
                    }
                }
            }
        }
    }


    public void setState(String s, boolean b) {
        MenuElement[] jme = mymenu.getSubElements();
        for (int i = 0; i < jme.length; i++) {
            Object mi = jme[i];
            if (mi instanceof DCheckboxMenuItem) {
                DCheckboxMenuItem tmi = (DCheckboxMenuItem)mi;
                String sm = tmi.getText();
                if (sm.equals(s))
                    tmi.setState(b);
            }
        }

    }


    public void addItem(String s) {
        addItem(s, false);
    }


    public void addItem(String s, boolean b) {
        if (type == SIMPLE) {
            DMenuItem tmi = new DMenuItem(s);
            // tmi.setLabelActor(this);
            mymenu.add(tmi);

        } else if (type == CHECKBOX) {
            DCheckboxMenuItem tmi = new DCheckboxMenuItem(s, this, b);
            // tmi.setLabelActor(this);
            mymenu.add(tmi);
        }
    }


    public void addItems(String[] sa) {
        if (sa == null)
            return;
        for (int i = 0; i < sa.length; i++)
            addItem(sa[i]);
    }


    public void addCheckboxItem(String s, boolean b) {
        DCheckboxMenuItem tmi = new DCheckboxMenuItem(s, this, b);
        tmi.setLabelActor(this);
        tmi.setActionCommand(s);
        // tmi.addActionListener (this);
        // tmi.addItemListener (this);
        mymenu.add(tmi);
    }


    public void addSeparator() {
        mymenu.addSeparator();
    }


    public void removeAll() {
        options = null;
        mymenu.removeAll();
    }


    public void addHierarchical(String[] sain, boolean b) {
        String[] sa = sain;
        if (sa == null)
            return;
        if (b)
            sa = myStringSort(sa);
        addHierarchical(sa);
    }


    public void addOptions(ArrayList<String> v) {
        String[] sa = new String[v.size()];
        int iin = 0;
        for (String s : v) {
            sa[iin++] = s;
        }
        addMenuOptions(sa);
    }


    public void addMenuOptions(String[] sa) {
        addHierarchical(sa);
    }


    public void setMenued(Menued md) {
        menued = md;
        setMenuOptions(menued.getMenuOptions());
    }


    public void setOptions(ArrayList<String> v) {
        setMenuOptions(v);
    }


    public void setMenuOptions(ArrayList<String> v) {
        String[] sa = new String[v.size()];
        int iin = 0;
        for (String s : v) {
            sa[iin++] = s;
        }
        setMenuOptions(sa);
    }



    public void setMenuOptions(String[] sa) {
        removeAll();
        options = sa;
        addHierarchical(sa);
    }


    public void addHierarchical(String[] sa) {
        growMenu(mymenu, "", sa, 0);
    }


    private int growMenu(Object tpm, String schop, String[] sa, int i0) {
        int nend = sa.length;
        int nchop = schop.length();
        int nn = 0;
        while (i0 + nn < nend && sa[i0 + nn] != null && sa[i0 + nn].startsWith(schop)) {
            String ss = sa[i0 + nn];
            String srest = ss.substring(nchop, ss.length());
            int inxt = srest.indexOf(":");
            if (inxt < 0)
                inxt = srest.indexOf("/");

            if (inxt > 1) {
                JMenu ttpm = new JMenu(srest.substring(0, inxt));

                if (tpm instanceof JPopupMenu)
                    ((JPopupMenu)tpm).add(ttpm);
                if (tpm instanceof JMenu)
                    ((JMenu)tpm).add(ttpm);


                nn += growMenu(ttpm, schop + srest.substring(0, inxt + 1), sa, i0 + nn);
            } else {
                if (srest.equals("SEPARATOR")) {
                    if (tpm instanceof JPopupMenu) {
                        ((JPopupMenu)tpm).addSeparator();
                    } else if (tpm instanceof JMenu) {
                        ((JMenu)tpm).addSeparator();
                    }

                } else {
                    DMenuItem tmi = new DMenuItem(srest);
                    tmi.setActionCommand(schop + srest);
                    // tmi.setLabelActor (this);
                    if (tpm instanceof JPopupMenu)
                        ((JPopupMenu)tpm).add(tmi);
                    if (tpm instanceof JMenu)
                        ((JMenu)tpm).add(tmi);
                }
                nn++;
            }
        }
        return nn;
    }



    public void addAlphabeticizedSubsetted(String[] sain) {
        String[] sa = sain;
        sa = myStringSort(sa);
        // chop inti submenus of no more than nmax elements, giving
        // each a title like "a-e";

        int nmin = 5;
        int nmax = 15;
        int ndone = 0;

        int ntot = sa.length;

        while (ndone < ntot) {

            // find how many letters need to get a change in the right place;
            int nlet = 0;
            boolean ok = false;

            int lstcng = 0;
            while (!ok && nlet < 3) {
                nlet++;
                lstcng = 0;
                String s0 = sa[ndone].substring(0, nlet);

                for (int i = 0; i < nmax && ndone + i < ntot; i++) {
                    String sn = sa[ndone + i].substring(0, nlet);
                    if (!(sn.equals(s0))) {
                        lstcng = i - 1;
                        s0 = sn;
                    } else if (ndone + i == ntot - 1) {
                        lstcng = i;
                    }
                }
                if (ndone + lstcng == ntot - 1 || lstcng >= nmin) {
                    ok = true;
                }
            }


            if (!ok) {
                lstcng = nmax; // couldnt find a break, just use nmax;
                if (ndone + lstcng >= ntot)
                    lstcng = ntot - ndone - 1;
            }

            String shead = (sa[ndone].substring(0, nlet) + "-" + sa[ndone + lstcng].substring(0, nlet) + ":");
            for (int i = 0; i <= lstcng; i++) {
                sa[ndone + i] = shead + sa[ndone + i];
            }
            ndone += lstcng;
            ndone++;
        }
        addHierarchical(sa);
    }



    private String[] myStringSort(String[] sa) {
        int n = sa.length;
        String[] ss = new String[n];
        for (int i = 0; i < n; i++) {
            String sin = sa[i];
            int j = 0;
            for (; j < i && ss[j].compareTo(sin) < 0; j++)
                ;
            for (int k = i; k > j; k--)
                ss[k] = ss[k - 1];
            ss[j] = sin;
        }
        return ss;
    }
}



// add should just add a menu item and set its action listener to this
// add (string1, string2) should add an item called string1 with the
// action command set to string1 + " " + string2



