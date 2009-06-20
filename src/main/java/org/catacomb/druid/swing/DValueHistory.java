package org.catacomb.druid.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.SpecialStrings;



public class DValueHistory extends JButton implements DComponent,
    ActionListener, LabelActor, Icon, MouseListener {

    static final long serialVersionUID = 1001;

    LabelActor lact;

    JPopupMenu menu;

    ArrayList<String> values = new ArrayList<String>();

    RolloverEffect rollover;



    public DValueHistory() {
        this(new String[0]);
    }

    public DValueHistory(String[] vals) {
        super("");
        setIcon(this);

        rollover = new RolloverEffect(this);
        addMouseListener(rollover);
        // setBorder(BorderFactory.createEtchedBorder());


        addActionListener(this);
        addMouseListener(this);
        setFocusPainted(false);


        setOptions(vals);
    }



    public void setBg(Color c) {
        setBackground(c);
        menu.setBackground(c);
        rollover.setBg(c);
    }




    public void checkContains(String s) {
        if (!values.contains(s)) {
            values.add(0, s);
            checkSize();
            updateOptions();
        }
    }



    public void setOptions(String[] vals) {
        for (String s : vals) {
            if (values.contains(s)) {
                // OK
            } else {
                values.add(0, s);
            }
        }
        checkSize();
        updateOptions();
    }


    private void checkSize() {
        if (values.size() > 12) {
            for (int i = values.size()-1; i >= 12; i--) {
                values.remove(i);
            }
        }
    }

    public void updateOptions() {
        menu = new JPopupMenu();

        DMenuItem dminone = new DMenuItem(SpecialStrings.NONE_STRING);
        dminone.setLabelActor(this);
        menu.add(dminone);
        menu.addSeparator();

        for (String s : values) {
            DMenuItem dmi = new DMenuItem(s);
            dmi.setLabelActor(this);
            menu.add(dmi);

        }

    }


    public void labelAction(String s, boolean b) {
        deliverAction(s, true);
    }



    public void actionPerformed(ActionEvent aev) {
        // System.out.println("dchoice action event");
        // menu.show(this, 0, 18);
    }

    @SuppressWarnings("unused")
    public void stateChanged(ChangeEvent cev) {

    }


    public void showMenu() {
        menu.show(this, 0, 18);
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



    public void setTooltip(String s) {
        // TODO Auto-generated method stub

    }


}
