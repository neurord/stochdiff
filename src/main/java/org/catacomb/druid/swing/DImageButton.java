package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.icon.IconLoader;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;


public class DImageButton extends JButton implements ActionListener {

    static final long serialVersionUID = 1001;



    String actionCommand;

    RolloverEffect rollover;

    LabelActor lact;



    public DImageButton(String image, String tooltip) {
        super();

        setToolTipText(tooltip);

        Icon icon = IconLoader.createImageIcon(image);
        if (icon == null) {
            setText("err");
        } else {
            setIcon(icon);
        }

        setFont(new Font("sansserif", Font.PLAIN, 12));

        rollover = new RolloverEffect(this, RolloverEffect.NONE, RolloverEffect.RAISED);

        addMouseListener(rollover);
        // setBorder(BorderFactory.createEtchedBorder());

        // setFont(new Font ("sansserif", Font.PLAIN, 12));
        // setToolTipText(infoText);

        addActionListener(this);
        setFocusPainted(false);

    }



    public void setBg(Color c) {
        setBackground(c);
    }


    public void setActionCommand(String s) {
        actionCommand = s;
    }


    public void setLabelActor(LabelActor la) {
        lact = la;
    }


    public void actionPerformed(ActionEvent aev) {
        if (lact == null) {
            lact.labelAction(actionCommand, true);
        }
    }


    public String toString() {
        return "DImageButton ";
    }



}
