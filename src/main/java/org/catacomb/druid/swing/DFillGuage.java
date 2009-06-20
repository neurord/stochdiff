package org.catacomb.druid.swing;


import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.MouseActor;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JProgressBar;



public class DFillGuage extends JProgressBar implements DComponent, MouseListener {
    static final long serialVersionUID = 1001;

    static int imax = 200;

    static Font plainfont;

    MouseActor mouseActor;


    public DFillGuage() {
        super(0, imax);
        setBorderPainted(false);
        setPlainFont();
    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }


    public void setBg(Color c) {
        setBackground(c);

        setBorder(BorderUtil.makeBorder(BorderUtil.ETCHED_UP, c));

    }




    public void showValue(double d, String s) {
        setValue((int)(imax * d));
        setString(s);
    }




    public void setPlainFont() {
        if (plainfont == null) {
            plainfont = new Font("sansserif", Font.PLAIN, 12);
        }

        setFont(plainfont);
    }

    /*

    public void setBoldFont() {
       if (boldfont == null) {
     boldfont = new Font ("sansserif", Font.BOLD, 12);
       }
       setFont(boldfont);
    }
    */


    public void setMouseActor(MouseActor ma) {
        if (mouseActor == null) {
            addMouseListener(this);
        }
        mouseActor = ma;
    }

    public void mousePressed(MouseEvent me) {
        mouseActor.mouseButtonPressed();
    }

    public void mouseEntered(MouseEvent me) {}
    public void mouseExited(MouseEvent me) {}
    public void mouseReleased(MouseEvent me) {}
    public void mouseClicked(MouseEvent me) {}




}
