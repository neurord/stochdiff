package org.catacomb.druid.swing;

import org.catacomb.icon.DImageIcon;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.MouseActor;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;


public class DLabel extends JLabel implements DComponent {
    static final long serialVersionUID = 1001;

    static Font plainfont;

    static Font boldfont;


    MouseActor mouseActor;
    String clickReferent;

    public DLabel(DImageIcon dic) {
        super(dic);
    }

    public DLabel(String s, int pos) {
        super(s, pos);
        setPlainFont();
    }

    public DLabel(String s) {
        super(s);

        setPlainFont();
    }

    public void setTooltip(String s) {
        setToolTipText(s);
    }

    public void setBg(Color c) {
        setBackground(c);
    }


    public void setFg(Color c) {
        setForeground(c);
    }



    public void setToolTip(String s) {
        setToolTipText(s);
    }



    public void setMouseActor(MouseActor ma) {
        addMouseListener(new DMouseRelay(ma));
    }



    public void setPlainFont() {
        if (plainfont == null) {
            plainfont = new Font("sansserif", Font.PLAIN, 12);
        }

        setFont(plainfont);
    }


    public void setFontBold() {
        setBoldFont();
    }

    public void setBoldFont() {
        if (boldfont == null) {
            boldfont = new Font("sansserif", Font.BOLD, 12);
        }
        setFont(boldfont);
    }

}
