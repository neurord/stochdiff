package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;
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


public class DFloatSlider extends JPanel
    implements MouseSource, MouseListener, MouseMotionListener {

    static final long serialVersionUID = 1001;


    DFloat dFloat;

    LabelActor labelActor;


    double value;


    int iscale;

    int state;
    final static int NONE = 0;
    final static int DRAG = 1;


    double linmin;
    double linmax;
    double linvalue;

    String label;

    int xslider;



    int xdown;
    int ydown;
    double linvaluedown;
    long downtime;


    Color bgColor;
    RolloverEffect rollover;


    public DFloatSlider(DFloat df, double v, double min, double max, int scl) {
        dFloat = df;

        addMouseListener(this);
        addMouseMotionListener(this);

        attachRollover();

        setScale(scl);
        setRange(min, max);
        setValue(v);
    }


    public void setScale(int isc) {
        iscale = isc;
    }


    public void setRange(double min, double max) {
        if (isLog()) {
            linmin = mylog(min);
            linmax = mylog(max);

        } else {
            linmin = min;
            linmax = max;
        }

    }


    public void setMouseActor(MouseActor ma) {
        addMouseListener(new DMouseRelay(ma));
    }


    public void setLabel(String s) {
        label = s;
    }


    public void setBg(Color col) {
        setBackground(col);
        bgColor = col;
        rollover.setBg(col);
    }


    public boolean isLog() {
        return (iscale == DFloat.LOG);
    }


    private double mylog(double vin) {
        double v = vin;
        double xmin = 1.e-99;
        if (v <= xmin) {
            v = xmin;
        }
        double ret = Math.log(v);
        return ret;
    }


    public void attachRollover() {
        rollover = new RolloverEffect(this);
        addMouseListener(rollover);
    }


    public void setLabelActor(LabelActor lact) {
        labelActor = lact;
    }

    /*
       private void notifyChange() {
          if (labelActor != null) {
             labelActor.labelAction("change", true);
          }
       }
    */

    public double getValue() {
        return value;
    }


    public void setValue(double d) {
        value = d;
        linvalue = toLin(d);
        ensureInRange();
    }


    public double toLin(double d) {
        double ret = d;
        if (isLog()) {
            ret = mylog(value);
        }
        return ret;
    }


    public double fromLin(double d) {
        double ret = d;
        if (isLog()) {
            ret = Math.exp(d);
        }
        return ret;
    }



    public void export() {
        value = fromLin(linvalue);
        // notifyChange();
        dFloat.setFromSlider(value);
    }



    private void ensureInRange() {
        if (linvalue < linmin) {
            linvalue = linmin;
        }
        if (linvalue > linmax) {
            linvalue = linmax;
        }
    }


    /*
    private void ensureRangeCovers() {
       if (linmin > linvalue) {
          linmin = linvalue;
       }
       if (linmax < linvalue) {
          linmax = linvalue;
       }
    }
    */


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

        if (label != null) {
            g.setColor(Color.black);
            g.drawString(label, 40, 20);
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

        double f = (linvalue - linmin) / (linmax - linmin);
        int icen = (int)(25 + f * (width - 50));
        if (icen < 25) {
            icen = 25;
        }
        if (icen > width - 25) {
            icen = width - 25;
        }
        drawUpButton(g, icen, hh, 5, 5);

        xslider = icen;
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
        nudge(-0.02);
    }


    public void nudgeRight() {
        nudge(0.02);
    }


    public void nudge(double f) {
        /*
         * if (integ) { linvalue += (f > 0 ? -1 : 1); } else {
         */

        linvalue += f * (linmax - linmin);


        ensureInRange();
        export();
        repaint();
    }


    @SuppressWarnings("unused")
    public void rsfMouseDown(int x, int y, long when, int button) {
        // requestFocus();

        xdown = x;
        ydown = y;
        linvaluedown = linvalue;
        downtime = when;

        state = NONE;


        // if (Math.abs (x - xslider) < 9) {

        if (x > 15 && x < getWidth() - 15) {
            state = DRAG;

        } else if (x < 15) {
            nudgeLeft();

        } else if (x > getWidth() - 15) {
            nudgeRight();
        }
    }


    @SuppressWarnings("unused")
    public void rsfMouseUp(int x, int y) {
    }



    public void rsfMouseDrag(int x, int y) {
        int height = getHeight();
        int width = getWidth();

        double f = 0.0;
        if (y > height + 40) {
            f = 0.01 * (height + 40 - y);
        } else if (y < -40) {
            f = 0.01 * (40 + y);
        }


        if (state == DRAG) {
            double dr = (Math.pow(10.0, f) * (x - xdown)) / (width - 30);
            linvalue = linvaluedown + dr * (linmax - linmin);
            ensureInRange();
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


    public double getTotalRange() {
        double ret = 1.0;
        if (isLog()) {
            ret = linmax - linmin;
        } else {
            ret = Math.pow(10., linmax) - Math.pow(10., linmin);  // EFF
        }
        return ret;
    }

}
