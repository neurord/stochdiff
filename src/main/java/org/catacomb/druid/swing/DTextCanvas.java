package org.catacomb.druid.swing;

import org.catacomb.interlish.content.IntPosition;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.TextPainter;

import java.awt.*;

import javax.swing.JPanel;

import javax.swing.Scrollable;



public class DTextCanvas extends JPanel implements DComponent, Scrollable {
    private static final long serialVersionUID = 1L;



    BasicStroke bs1 = new BasicStroke((float)1.0);

    Color bgColor = new Color(32, 232, 238);  // ADHOC
    boolean antialias;

    TextPainter textPainter;

    int txtHeight;

    DPanel parentPanel;

    public DTextCanvas() {
        setFont(new Font("sansserif", Font.PLAIN, 12));
        txtHeight=400;
    }



    public void setTooltip(String s) {
        setToolTipText(s);
    }




    public void setTextPainter(TextPainter tp) {
        textPainter = tp;
    }

    public void paintComponent(Graphics g0) {

        g0.setColor(bgColor);
        g0.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g = (Graphics2D)g0;

        if (antialias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        simpleStroke(g);

        paint2D(g);

    }

    final void simpleStroke(Graphics2D g) {
        g.setStroke(bs1);
    }

    public void paint2D(Graphics2D g) {

        if (textPainter != null) {
            textPainter.paintText(g);

            txtHeight = textPainter.getFullTextHeight();
        }


    }

    public void requestRepaint() {
        repaint();
    }

    public IntPosition getScreenPosition() {
        Point p = getLocationOnScreen();
        return new IntPosition((int)(p.getX()), (int)(p.getY()));
    }

    public void setAntialias(boolean b) {
        antialias = b;
        repaint();
    }


    public boolean containsPoint(IntPosition pos) {
        int x = pos.getX();
        int y = pos.getY();
        boolean ret = (x > 5 &&  y > 10 &&
                       x < getWidth() - 10 && y < getHeight() - 10);
        return ret;
    }



    public void contentSizeChanged() {
        invalidate();
        if (parentPanel != null) {
            parentPanel.validate();
        }
        repaint();
    }



    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), txtHeight);
    }


    public Dimension getPreferredScrollableViewportSize() {
        Dimension d = getPreferredSize();
        Dimension ret = d; // new Dimension((int)(d.getWidth()), txtHeight);
        return ret;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 25;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 25;
    }






    public void setParentContainer(DPanel dp) {
        parentPanel = dp;
    }







}


