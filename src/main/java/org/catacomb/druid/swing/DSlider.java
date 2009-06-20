package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.MouseActor;
import org.catacomb.interlish.structure.MouseSource;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;



// REF refactor DFloatSlider to extend this

public class DSlider extends JPanel implements DComponent, MouseSource, MouseListener,
    MouseMotionListener {

    static final long serialVersionUID = 1001;

    LabelActor labelActor;

    int nmax;
    int islider;

    String descriptionText;

    String[] values;

    int xdown;
    int ydown;
    long downtime;


    Color bgColor;
    RolloverEffect rollover;


    int state;
    final static int NONE = 0;
    final static int DRAG = 1;


    public DSlider() {
        this(1);
    }


    public DSlider(int n) {
        nmax = n;
        values = new String[n];
        for (int i = 0; i < n; i++) {
            values[i] = "" + i;
        }

        addMouseListener(this);
        addMouseMotionListener(this);

        attachRollover();
    }

    public void setTooltip(String s) {
        setToolTipText(s);
    }


    public void setNPoint(int npt) {
        nmax = npt;
        values = new String[nmax];
        for (int i = 0; i < nmax; i++) {
            values[i] = "" + i;
        }
        repaint();
    }


    public void setMouseActor(MouseActor ma) {
        addMouseListener(new DMouseRelay(ma));
    }


    public void setBg(Color col) {
        setBackground(col);
        bgColor = col;
        rollover.setBg(col);
    }


    public void attachRollover() {
        rollover = new RolloverEffect(this);
        addMouseListener(rollover);
    }


    public void setLabelActor(LabelActor lact) {
        labelActor = lact;
    }


    private void notifyChange() {
        if (labelActor != null) {
            labelActor.labelAction("change", true);
        }
    }


    public void export() {
        notifyChange();
    }


    public void setValues(String[] sa) {
        values = sa;
        nmax = sa.length;
        showValue(0);
    }


    public int getValue() {
        return islider;
    }



    public void showValue(int iv) {
        if (iv != islider) {
            islider = iv;
            repaint();
        }
    }


    public Dimension getMinimumSize() {
        return new Dimension(80, 20);
    }


    public Dimension getPreferredSize() {
        return new Dimension(140, 22);
    }



    public void paintComponent(Graphics g) {
        realPaint(g);
    }



    public void realPaint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (bgColor == null) {
            bgColor = getBackground();
        }
        g.setColor(bgColor);
        g.fillRect(0, 0, w, h);

        paintArrows(g);

        paintKnob(g);

        if (descriptionText != null) {
            g.setColor(Color.black);
            g.drawString(descriptionText, 40, h-5);
        }
    }



    private void paintArrows(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        Color cbg = bgColor;
        Color cbr = cbg.brighter();
        Color cdk = cbg.darker();

        int hh = h / 2;
        g.setColor(cbr);
        g.drawLine(4, hh, 15, 4);

        g.drawLine(w - 15, h - 4, w - 15, 4);

        // g.drawLine(w-15, 4, w-10, hh);


        g.setColor(cdk);
        g.drawLine(15, 4, 15, h - 4);
        // g.drawLine(15, h-4, 10, hh);


        g.drawLine(4, hh, 15, h - 4);

        g.drawLine(w - 15, h - 4, w - 4, hh);
        g.drawLine(w - 15, 4, w - 4, hh);

    }


    private void paintKnob(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int hh = height / 2;

        if (nmax < 1) {
            nmax = 1;
        }

        if (islider >= nmax) {
            islider = nmax - 1;
        }

        double f = (islider + 0.5) / nmax;

        int icen = (int)(25 + f * (width - 50));
        if (icen < 25) {
            icen = 25;
        }
        if (icen > width - 25) {
            icen = width - 25;
        }
        drawUpButton(g, icen, hh, 5, 5);

        if (islider >= 0 && values != null && islider < values.length && values[islider] != null) {
            g.setColor(Color.black);
            if (icen < 80) {
                g.drawString(values[islider], 100, 18);
            } else {
                g.drawString(values[islider], 30, 18);
            }
        }

    }


    private void drawUpButton(Graphics g, int icx, int icy, int hw, int hh) {
        Color c = getBackground();

        g.setColor(c.darker());
        g.drawLine(icx - hw - 1, icy + hh + 1, icx + hw + 1, icy + hh + 1);
        g.drawLine(icx - hw, icy + hh, icx + hw, icy + hh);

        g.drawLine(icx + hw + 1, icy - hh - 1, icx + hw + 1, icy + hh + 1);
        g.drawLine(icx + hw, icy - hh, icx + hw, icy + hh);


        g.setColor(c.brighter());
        g.drawLine(icx - hw - 1, icy - hh - 1, icx + hw + 1, icy - hh - 1);
        g.drawLine(icx - hw, icy - hh, icx + hw, icy - hh);

        g.drawLine(icx - hw - 1, icy - hh - 1, icx - hw - 1, icy + hh + 1);
        g.drawLine(icx - hw, icy - hh, icx - hw, icy + hh);



    }


    public void nudgeLeft() {
        if (islider > 0) {
            islider = islider - 1;
            repaint();
            export();
        }
    }


    public void nudgeRight() {
        if (islider < nmax - 1) {
            islider = islider + 1;
            repaint();
            export();
        }
    }


    @SuppressWarnings("unused")
    public void rsfMouseDown(int x, int y, long when, int button) {

        state = NONE;
        if (x > 15 && x < getWidth() - 15) {
            state = DRAG;
            moveTo(x);

        } else if (x < 15) {
            nudgeLeft();

        } else if (x > getWidth() - 15) {
            nudgeRight();
        }
    }


    @SuppressWarnings("unused")
    public void rsfMouseUp(int x, int y) {
    }


    @SuppressWarnings("unused")
    public void rsfMouseDrag(int x, int y) {
        if (state == DRAG) {
            moveTo(x);
        }
    }


    private void moveTo(int x) {
        int width = getWidth();


        double frel = (x - 15.) / (width - 30.);
        int oslider = islider;
        islider = (int)(frel * nmax + 0.0);
        if (islider < 0) {
            islider = 0;
        }
        if (islider > nmax - 1) {
            islider = nmax - 1;
        }
        if (islider != oslider) {
            export();
            repaint();
        }
    }



    // map listeners to rfsXXX methods;

    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        rsfMouseDrag(x, y);
    }


    public void mouseMoved(MouseEvent e) {
    }


    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        long when = e.getWhen();
        int modif = e.getModifiers();
        int button = 0;
        if (modif == InputEvent.BUTTON1_MASK) {
            button = 1;
        } else if (modif == InputEvent.BUTTON2_MASK) {
            button = 2;
        } else if (modif == InputEvent.BUTTON3_MASK) {
            button = 3;
        }
        rsfMouseDown(x, y, when, button);
    }



    public void mouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        rsfMouseUp(x, y);
    }



    public void mouseEntered(MouseEvent e) {
        requestFocus();
    }


    public void mouseExited(MouseEvent e) {
    }


    public void mouseClicked(MouseEvent e) {
        requestFocus();
    }


    public void pointShown(int ifr, String desc) {
        descriptionText = desc;
        showValue(ifr);
    }

}
