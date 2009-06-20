package org.catacomb.druid.swing;


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.report.E;



public class DRadioButtons extends DPanel implements ActionListener {

    static final long serialVersionUID = 1001;

    LabelActor lact;

    String selected;

    String[] options;
    String[] labels;

    JRadioButton[] buttons;

    Font labelFont;

    CardLayout cardLayout;

    boolean vertical = true;

    Color bgColor;

    public DRadioButtons(String[] opts, String layout) {
        super();

        if (layout == null || layout.equals("vertical")) {
            vertical = true;
        } else if (layout.equals("horizontal")) {
            vertical = false;
        } else {
            E.warning("unknown layout: " + layout);
            vertical = false;
        }

        labelFont = new Font("sansserif", Font.PLAIN, 12);

        setOptions(opts, opts);

    }

    public void setBg(Color c) {
        bgColor = c;
        super.setBg(c);
        for (JRadioButton jrb : buttons) {
            jrb.setBackground(c);
        }
    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }


    public void setOptions(String[] optsin, String[] labsin) {
        removeAll();

        String[] opts = optsin;
        String[] labs = labsin;

        if (opts == null) {
            opts = new String[0];
        }

        options = opts;
        labels = labs;

        int nopt = opts.length;
        if (vertical) {
            setLayout(new GridLayout(nopt, 1, 2, 2));
        } else {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
        }

        ButtonGroup bgp = new ButtonGroup();

        buttons = new JRadioButton[nopt];

        for (int i = 0; i < nopt; i++) {
            JRadioButton jrb = new JRadioButton(labels[i]);
            jrb.setActionCommand(options[i]);
            // jrb.setBackground(bgColor);
            jrb.setFont(labelFont);

            bgp.add(jrb);
            buttons[i] = jrb;
            jrb.addActionListener(this);
            add(jrb);
            if (bgColor != null) {
                jrb.setBackground(bgColor);
            }
        }


        revalidate();
    }


    public void setSelectedIndex(int isel) {
        for (int i = 0; i < buttons.length; i++) {
            if (isel != i && buttons[i].isSelected()) {
                buttons[i].setSelected(false);
            } else if (isel == i && !buttons[i].isSelected()) {
                buttons[i].setSelected(true);
            }
        }
    }


    public void setSelected(String s) {
        if (s == null) {
            for (int i = 0; i < options.length; i++) {
                if (buttons[i].isSelected()) {
                    buttons[i].setSelected(false);
                }
            }


        } else {

            boolean done = false;
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(s)) {
                    selected = s;
                    if (!buttons[i].isSelected()) {
                        buttons[i].setSelected(true); // could call doClick to have an
                        // event generated;
                    }
                    done = true;
                }
            }
            if (!done) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < options.length; i++) {
                    sb.append(options[i] + ", ");
                }
                E.error("cant set selected  - not an option " + s + " possibilities are: " + sb.toString());
            }
        }
    }


    public String getSelected() {
        return selected;
    }



    public void actionPerformed(ActionEvent aev) {
        selected = aev.getActionCommand();
        deliverAction(selected, true);

    }


    public void setLabelActor(LabelActor bl) {
        lact = bl;
    }



    public void deliverAction(String s, boolean b) {
        if (lact != null) {
            lact.labelAction(s, b);
        }
    }


    /*
       public void setCardLayout() {
         cardLayout = new CardLayout();
         setLayout(cardLayout);
       }



       public void nextCard() {
          cardLayout.next(this);
       }



       public void showCard(String s) {
          cardLayout.show(this, s);
       }

    */

}
