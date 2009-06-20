package org.catacomb.druid.swing;

import org.catacomb.interlish.structure.MouseSource;
import org.catacomb.util.ColorUtil;


import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.border.Border;


public class DToggleButton extends DBaseButton implements ActionListener, MouseListener,
    MouseSource {

    boolean state;

    Color bgColor;

    private ImageIcon offIcon;
    private ImageIcon onIcon;
    static final long serialVersionUID = 1001;

    boolean pressOn;
    boolean mouseOn;

    int padLeft;
    int padRight;
    int padTop;
    int padBottom;


    public DToggleButton(String lab) {
        super();
        if (lab != null && lab.length() > 0) {
            setText(lab);
        }
        label = lab;

        setFont(new Font("sansserif", Font.PLAIN, 12));

        addActionListener(this);
        setFocusPainted(false);
        addMouseListener(this);
    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }

    public void setBg(Color c) {
        bgColor = c;
        setBackground(c);
    }


    public void setFg(Color c) {
        setForeground(c);
    }



    public String toString() {
        return "DToggleButton " + label;
    }

    public void suggest() {
        // TODO - is this needed?
    }


    public String getStringIdentifier() {
        return label;
    }



    public void setPadding(int p) {
        setPadding(p, p, p, p);
    }



    public void actionPerformed(ActionEvent aev) {
        pressOn = !pressOn;
        applyState();
        deliverAction(pressOn);
    }


    public void setState(boolean b) {
        pressOn = b;
        applyState();
    }


    public void applyState() {
        Color cbg = bgColor;
        Color cbgd = ColorUtil.darker(cbg);
        Color cbgb = ColorUtil.brighter(cbg);

        Border b = null;

        if (pressOn) {
            setBackground(cbgd);
            if (mouseOn) {
                b = BorderUtil.makeEtchedUpBorder(cbgd);
            } else {
                b = BorderUtil.makeSunkenBorder(cbgd, cbg);
            }

        } else {
            setBackground(cbg);
            if (mouseOn) {
                b = BorderUtil.makeEtchedUpBorder(cbg);

            } else {
                b = BorderUtil.makeRaisedBorder(cbg, cbgb);
            }
        }

        Border bpad = BorderUtil.makeEmptyBorder(padLeft, padRight, padTop, padBottom);
        Border btot = BorderUtil.makeCompoundBorder(bpad, b);
        setBorder(btot);


        if (pressOn) {
            if (onIcon != null) {
                setIcon(onIcon);
            }
        } else {
            if (offIcon != null) {
                setIcon(offIcon);
            }
        }
    }



    public void setOnIcon(ImageIcon icon) {
        onIcon = icon;
        if (state) {
            applyState();
        }
    }


    public void setOffIcon(ImageIcon icon) {
        offIcon = icon;
        if (!state) {
            applyState();
        }
    }


    public void mouseClicked(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
    }


    public void mouseReleased(MouseEvent e) {
    }


    public void mouseEntered(MouseEvent e) {
        mouseOn = true;
        applyState();
    }


    public void mouseExited(MouseEvent e) {
        mouseOn = false;
        applyState();
    }


    public void setPadding(int pl, int pr, int pt, int pb) {
        padLeft = pl;
        padRight = pr;
        padTop = pt;
        padBottom = pb;
        applyState();
    }


    @Override
    public void deSuggest() {
        // TODO Auto-generated method stub

    }

}
