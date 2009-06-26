package org.catacomb.druid.swing;

import org.catacomb.druid.dialog.Dialoguer;
import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.event.OptionsSource;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.lang.U;
import org.catacomb.interlish.structure.SpecialStrings;
import org.catacomb.interlish.structure.Updatable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;



public class DChoice extends JButton implements DComponent,
    ActionListener, LabelActor, Icon, MouseListener {

    static final long serialVersionUID = 1001;

    LabelActor lact;

    JPopupMenu menu;

    String[] options;
    String[] labels;

    String selected;

    RolloverEffect rollover;

    OptionsSource optionsSource;
    Updatable updatable;

    int autoSelect;


    String prefTooltip = null;


    public DChoice(String[] opts, String[] labs) {
        super("none");
        setIcon(this);


        setFont(new Font("sansserif", Font.PLAIN, 12));


        rollover = new RolloverEffect(this);
        addMouseListener(rollover);
        // setBorder(BorderFactory.createEtchedBorder());


        addActionListener(this);
        addMouseListener(this);
        setFocusPainted(false);


        setOptions(opts, labs);
        autoSelect = -1;
    }


    public void setTooltip(String s) {
        prefTooltip = s;
        setToolTipText(s);
    }


    public void setAutoSelect(int ias) {
        autoSelect = ias;
        progSelect();
    }


    public void setBg(Color c) {
        setBackground(c);
        menu.setBackground(c);
        rollover.setBg(c);
    }


    public void setOptionsSource(OptionsSource os) {
        optionsSource = os;
    }


    public void setOptions(String[] optsin, String[] labsin) {
        String[] opts = optsin;
        String[] labs = labsin;

        // actively discard old ones ???;
        if (opts == null) {
            opts = new String[0];
            labs = new String[0];
        }

        if (labs == null || labs.length == 0) {
            labs = new String[opts.length];
        } else if (labs.length < opts.length) {
            String[] olabs = labs;
            labs = new String[opts.length];
            for (int i = 0; i < olabs.length; i++) {
                labs[i] = olabs[i];
            }
        }

        for (int i = 0; i < opts.length; i++) {
            if (labs[i] == null) {
                labs[i] = opts[i];
            }
        }


        boolean stillIn = false;
        options = opts;
        labels = labs;
        menu = new JPopupMenu();

        DMenuItem dminone = new DMenuItem(SpecialStrings.NONE_STRING);
        dminone.setLabelActor(this);
        menu.add(dminone);
        menu.addSeparator();

        for (int i = 0; i < opts.length; i++) {
            if (opts[i].equals(selected)) {
                stillIn = true;
            }

            DMenuItem dmi = new DMenuItem(labs[i], opts[i]);
            dmi.setLabelActor(this);
            menu.add(dmi);

        }



        if (selected != null && stillIn) {
            // no change;
            // } else if (opts.length > 0) {
            // setSelected(opts[0]);
        } else {

            progSelect();
        }
    }



    private void progSelect() {
        if (autoSelect >= 0 && options != null && options.length > autoSelect) {
            labelAction(options[autoSelect], true);

        } else {
            // POSERR  should this also generate a label action?
            setSelected(null);
        }
    }



    public void checkOptions() {
        if (updatable != null) {
            updatable.update(0);
        }

        if (optionsSource != null) {
            String[] sa = optionsSource.getOptions();
            if (sa == null) {
                sa = new String[0];
            }
            String[] sb = optionsSource.getLabels();
            boolean neednew = false;
            if (options == null || options.length != sa.length) {
                neednew = true;
            } else {
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(sa[i])) {

                    } else {
                        neednew = true;
                        break;
                    }
                }
            }

            if (neednew) {
                setOptions(sa, sb);
            }
        }
    }



    public void setSelected(String s) {

        if (selectOne(s)) {
            // OK;

        } else if (s == null || s.equals("") || s.equals("null") ||
                   s.equals(SpecialStrings.NONE_STRING)) {
            setText(SpecialStrings.NONE_STRING);
            selected = SpecialStrings.NONE_STRING;


        } else {
            checkOptions();
            if (selectOne(s)) {
                // OK;
            } else {
                String msg = "Warning the selection " + s + "\n" +
                             "is no longer available. The selection will be unset -b-" +
                             "The possible values are: -b-";

                for (String sopt : options) {
                    msg += sopt + "-b-";
                }
                Dialoguer.message(msg);
            }
        }
    }



    private boolean selectOne(String s) {
        boolean ok = false;

        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(s)) {
                ok = true;
                selected = s;

                String otxt = getText();

                if (U.differ(otxt, labels[i])) {
                    String sl = labels[i];
                    int sll = sl.length();
                    if (sll > 22) {
                        // override the normal tooltip, if present;
                        setToolTipText(sl);
                        sl = "..." + sl.substring(sll-19, sll);
                        setText(sl);
                    } else {
                        // restore anything set by setTooltip;
                        setToolTipText(prefTooltip);
                        setText(sl);
                    }
                }

                //  E.info("replaced " + otxt + "  with " + s + " in " + this.hashCode());
                break;
            }
        }
        return ok;
    }



    public String getSelected() {
        return selected;
    }



    public void labelAction(String s, boolean b) {
        setSelected(s);
        deliverAction(s, true);
    }



    public void actionPerformed(ActionEvent aev) {
        // System.out.println("dchoice action event");
        // menu.show(this, 0, 18);
    }

    public void stateChanged(ChangeEvent cev) {

    }


    public void showMenu() {
        checkOptions();
        menu.show(this, 0, 18);
    }


    public String toString() {
        String sret = "DChoice";
        if (options != null && options.length > 0) {
            sret += " first opt=" + options[0];
        }
        return sret;
    }



    public void setLabelActor(LabelActor bl) {
        lact = bl;
    }



    public void deliverAction(String s, boolean b) {
        if (lact != null) {
            lact.labelAction(s, b);
        }
    }



    // icon methods to draw the button;
    public void paintIcon(Component c, Graphics g, int x, int y) {
        JComponent component = (JComponent)c;
        int iconWidth = getIconWidth();

        g.translate(x, y);

        g.setColor(component.isEnabled() ? Color.gray : Color.blue);

        g.drawLine(2, 0, iconWidth - 1, 0);
        g.drawLine(3, 1, 1 + (iconWidth - 3), 1);
        g.drawLine(5, 3, 3 + (iconWidth - 7), 3);
        g.drawLine(6, 4, 4 + (iconWidth - 9), 4);

        g.translate(-x, -y);
    }


    public int getIconWidth() {
        return 12;
    }


    public int getIconHeight() {
        return 5;
    }


    public void setUpdatable(Updatable u) {
        updatable = u;
    }




    public void mouseClicked(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
        showMenu();
    }


    public void clearSelection() {
        setSelected(null);

    }

}
