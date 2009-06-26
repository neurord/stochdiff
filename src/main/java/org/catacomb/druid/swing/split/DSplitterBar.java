package org.catacomb.druid.swing.split;

import org.catacomb.interlish.interact.DComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DSplitterBar extends JPanel implements DComponent {

    private static final long serialVersionUID = 1L;
    static final Cursor VERT_CURSOR = new Cursor(Cursor.N_RESIZE_CURSOR);
    static final Cursor HORIZ_CURSOR = new Cursor(Cursor.E_RESIZE_CURSOR);
    static final Cursor DEF_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    private int orientation = SplitterLayout.VERTICAL;

    private boolean alreadyDrawn = false;
    private Rectangle originalBounds = null;
    private Window wBar;


    public DSplitterBar() {
        addMouseMotionListener(new SplitterBarMouseMotionListener(this));
        addMouseListener(new SplitterBarMouseListener(this));
    }

    private void checkOtherComponents() {
        Rectangle currBounds = getBounds();  // get current position
        Component comps[] = getParent().getComponents();
        Insets insets = getParent().getInsets();
        Rectangle parentBounds = getParent().getBounds();

        // determine which component "this" is
        int curr;
        for (curr = 0; (curr<comps.length) && (comps[curr]!=this); curr++) ;
        int origCurr = curr; // hold for part II check

        if (orientation==SplitterLayout.VERTICAL) {
            if (currBounds.y<originalBounds.y) { // moved up
                // could have moved _into_ splitter bars above (or top edge)
                //   and/or away from splitter bars below (or bottom edge)

                // check to see it we've bumped into a splitter above us.
                boolean done = false;
                for (int temp = curr-1; !done && temp>-1; temp--) {
                    if (comps[temp] instanceof DSplitterBar) {
                        Rectangle r = comps[temp].getBounds();
                        if (currBounds.y<=r.y+r.height) { // touching or above...
                            comps[temp].setLocation(r.x, currBounds.y-r.height);
                            // any comps in between should be hidden
                            for (int c = curr-1; c>temp; c--)
                                comps[c].setVisible(false);
                            curr = temp;
                            currBounds = comps[temp].getBounds();
                        } // touching or above
                        else
                            done = true; // no more compression
                    } // it's a splitter bar
                } // for each component before us

                // did we push to far?
                if (currBounds.y<=insets.top) {
                    int delta = currBounds.y-insets.top;
                    // hide all components before that one
                    for (int temp = curr-1; temp>-1; temp--)
                        comps[temp].setVisible(false);
                    // push all splitter bars into view
                    done = false;
                    for (int temp = curr; !done && temp<=origCurr; temp++)
                        if (comps[temp] instanceof DSplitterBar) {
                            Point p = comps[temp].getLocation();
                            p.y -= delta;
                            comps[temp].setLocation(p);
                        } else
                            done = comps[temp].isVisible();
                } // pushed highest component off top edge

                // next, check if we exposed components below us
                curr = origCurr;
                // if the next component is not visible, show all between us & next
                //    splitter bar or bottom edge
                for (int temp = curr+1; temp<comps.length && !comps[temp].isVisible(); temp++)
                    comps[temp].setVisible(true);
            } // VERTICAL -- moved up
            else if (currBounds.y>originalBounds.y) { // moved down
                // could have moved _into_ splitter bars below (or bottom edge)
                //   and/or away from splitter bars above (or top edge)

                // check to see it we've bumped into a splitter below us.
                boolean done = false;
                for (int temp = curr+1; !done && temp<comps.length; temp++) {
                    if (comps[temp] instanceof DSplitterBar) {
                        Rectangle r = comps[temp].getBounds();
                        if (currBounds.y+currBounds.height>= r.y) { // touching or below...
                            comps[temp].setLocation(r.x, currBounds.y+currBounds.height);
                            // any comps in between should be hidden
                            for (int c = curr+1; c<temp; c++)
                                comps[c].setVisible(false);
                            curr = temp;
                            currBounds = comps[temp].getBounds();
                        } // touching or above
                        else
                            done = true; // no more compression
                    } // it's a splitter bar
                } // for each component before us

                // did we push to far?
                if ((currBounds.y+currBounds.height)>= (parentBounds.height-insets.bottom)) {
                    int delta = currBounds.y+currBounds.height-(parentBounds.height-insets.bottom);
                    // hide all components before that one
                    for (int temp = curr+1; temp<comps.length; temp++)
                        comps[temp].setVisible(false);
                    // push all splitter bars into view
                    done = false;
                    for (int temp = curr; !done && temp>= origCurr; temp--)
                        if (comps[temp] instanceof DSplitterBar) {
                            Point p = comps[temp].getLocation();
                            p.y -= delta;
                            comps[temp].setLocation(p);
                        } else
                            done = comps[temp].isVisible();
                } // pushed highest component off top edge

                // next, check if we exposed components below us
                curr = origCurr;
                // if the next component is not visible, show all between us & next
                //    splitter bar or bottom edge
                for (int temp = curr-1; temp>-1 && !comps[temp].isVisible(); temp--)
                    comps[temp].setVisible(true);
            } // VERTICAL -- moved down
        } // orientation==VERTICAL
        else { // orientation == HORIZONTAL
            if (currBounds.x<originalBounds.x) { // moved left
                // could have moved _into_ splitter bars to left (or left edge)
                //   and/or away from splitter bars to right (or right edge)

                // check to see it we've bumped into a splitter above us.
                boolean done = false;
                for (int temp = curr-1; !done && temp>-1; temp--) {
                    if (comps[temp] instanceof DSplitterBar) {
                        Rectangle r = comps[temp].getBounds();
                        if (currBounds.x<=r.x+r.width) { // touching or above...
                            comps[temp].setLocation(currBounds.x-r.width, r.y);
                            // any comps in between should be hidden
                            for (int c = curr-1; c>temp; c--)
                                comps[c].setVisible(false);
                            curr = temp;
                            currBounds = comps[temp].getBounds();
                        } // touching or above
                        else
                            done = true; // no more compression
                    } // it's a splitter bar
                } // for each component before us

                // did we push to far?
                if (currBounds.x<=insets.left) {
                    int delta = currBounds.x-insets.left;
                    // hide all components before that one
                    for (int temp = curr-1; temp>-1; temp--)
                        comps[temp].setVisible(false);
                    // push all splitter bars into view
                    done = false;
                    for (int temp = curr; !done && temp<=origCurr; temp++)
                        if (comps[temp] instanceof DSplitterBar) {
                            Point p = comps[temp].getLocation();
                            p.x -= delta;
                            comps[temp].setLocation(p);
                        } else
                            done = comps[temp].isVisible();
                } // pushed highest component off top edge

                // next, check if we exposed components below us
                curr = origCurr;
                // if the next component is not visible, show all between us & next
                //    splitter bar or bottom edge
                for (int temp = curr+1; temp<comps.length && !comps[temp].isVisible(); temp++)
                    comps[temp].setVisible(true);
            } // HORIZONTAL -- moved left
            else if (currBounds.x>originalBounds.x) { // moved right
                // could have moved _into_ splitter bars to right (or right edge)
                //   and/or away from splitter bars to left (or left edge)

                // check to see it we've bumped into a splitter to our right us.
                boolean done = false;
                for (int temp = curr+1; !done && temp<comps.length; temp++) {
                    if (comps[temp] instanceof DSplitterBar) {
                        Rectangle r = comps[temp].getBounds();
                        if (currBounds.x+currBounds.width>= r.x) { // touching or to right...
                            comps[temp].setLocation(currBounds.x+currBounds.width, r.y);
                            // any comps in between should be hidden
                            for (int c = curr+1; c<temp; c++)
                                comps[c].setVisible(false);
                            curr = temp;
                            currBounds = comps[temp].getBounds();
                        } // touching or above
                        else
                            done = true; // no more compression
                    } // it's a splitter bar
                } // for each component before us

                // did we push to far?
                if ((currBounds.x+currBounds.width)>= (parentBounds.width-insets.right)) {
                    int delta = currBounds.x+currBounds.width-(parentBounds.width-insets.right);
                    // hide all components before that one
                    for (int temp = curr+1; temp<comps.length; temp++)
                        comps[temp].setVisible(false);
                    // push all splitter bars into view
                    done = false;
                    for (int temp = curr; !done && temp>= origCurr; temp--)
                        if (comps[temp] instanceof DSplitterBar) {
                            Point p = comps[temp].getLocation();
                            p.x -= delta;
                            comps[temp].setLocation(p);
                        } else
                            done = comps[temp].isVisible();
                } // pushed highest component off top edge

                // next, check if we exposed components below us
                curr = origCurr;
                // if the next component is not visible, show all between us & next
                //    splitter bar or bottom edge
                for (int temp = curr-1; temp>-1 && !comps[temp].isVisible(); temp--)
                    comps[temp].setVisible(true);
            } // HORIZONTAL -- moved right
        } // orientation==HORIZONTAL

    } // checkComponents()

    public int getOrientation() {
        return orientation;
    }

    void mouseDrag(MouseEvent e) {
        if (SplitterLayout.dragee==null)
            SplitterLayout.dragee = this;
        else if (SplitterLayout.dragee!=this)
            return;
        Component c = getParent();
        Point fl = c.getLocationOnScreen();
        while (c.getParent()!=null)
            c = c.getParent();
        if (!alreadyDrawn) {
            originalBounds = getBounds();
            wBar = new Window((Frame)c);
            wBar.setBackground(getBackground().darker());
        }
        Container cp = getParent();
        Dimension parentDim = cp.getSize();
        Point l = getLocationOnScreen();
        Insets insets = cp.getInsets();
        if (orientation==SplitterLayout.VERTICAL)
            parentDim.width -= insets.right+insets.left;
        else
            parentDim.height -= insets.top+insets.bottom;
        Rectangle r = getBounds(); // mouse event is relative to this...
        int x = l.x+(orientation==SplitterLayout.HORIZONTAL ? e.getX() : 0);
        int y = l.y+(orientation==SplitterLayout.VERTICAL ? e.getY() : 0);
        if (x<fl.x+insets.left)
            x = fl.x+insets.left;
        else if ((orientation==SplitterLayout.HORIZONTAL) && (x>fl.x+parentDim.width-r.width))
            x = fl.x+parentDim.width-r.width;
        if (y<fl.y+insets.top)
            y = fl.y+insets.top;
        else if ((orientation==SplitterLayout.VERTICAL) && (y>fl.y+parentDim.height-r.height))
            y = fl.y+parentDim.height-r.height;
        wBar.setBounds(x, y,
                       (orientation==SplitterLayout.HORIZONTAL) ? 3 : parentDim.width,
                       (orientation==SplitterLayout.VERTICAL) ? 3 : parentDim.height);
        if (!alreadyDrawn) {
            wBar.setVisible(true);
            alreadyDrawn = true;
        }
    }
    void mouseEnter(MouseEvent e) {
        if (SplitterLayout.dragee!=null) return;
        setCursor((orientation==SplitterLayout.VERTICAL) ? VERT_CURSOR : HORIZ_CURSOR);
        // mouseInside = true;
        invalidate();
        validate();
        repaint();
    }
    void mouseExit(MouseEvent e) {
        if (SplitterLayout.dragee!=null) return;
        setCursor(DEF_CURSOR);
        // mouseInside = false;
        invalidate();
        validate();
        repaint();
    }

    void mouseRelease(MouseEvent e) {
        if (alreadyDrawn) {
            if (SplitterLayout.dragee!=this) return;
            SplitterLayout.dragee = null;
            wBar.setVisible(false);
            wBar.dispose();
            wBar = null;
            alreadyDrawn = false;
            Rectangle r = getBounds(); // mouse event is relative to this...
            r.x += (orientation==SplitterLayout.HORIZONTAL ? e.getX() : 0);
            r.y += (orientation==SplitterLayout.VERTICAL ? e.getY() : 0);
            setLocation(r.x, r.y);
            setCursor(DEF_CURSOR);

            // check to see if we need to move other splitters and hide other
            // components that are controlled by the layout
            // First -- find what component this one is

            checkOtherComponents();
            // mouseInside = false;
            invalidate();
            getParent().validate();
            SplitterLayout.dragee = null;
        }
    }

    /**
     * Paints the image of a JSplitterBar.  If nothing was added to
     * the JSplitterBar, this image will only be a thin, 3D raised line that
     * will act like a handle for moving the JSplitterBar.
     * If other components were added the JSplitterBar, the thin 3D raised
     * line will onlty appear where JSplitterSpace components were added.
     */
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(getBackground());
//        if(mouseInside)
//            g.setColor(Color.yellow);
//        else
//            g.setColor(Colors.lightSkyBlue3);
        Component c[] = getComponents();
        if (c!=null && c.length>0)
            for (int i = 0; i<c.length; i++) {
                if (c[i] instanceof DSplitterSpace) {
                    // only draw boxes where JSplitterSpace components appear
                    Rectangle r = c[i].getBounds();
                    if (orientation==SplitterLayout.VERTICAL)
                        g.fill3DRect(r.x+2, r.y+r.height/2-1, r.width-5, 3, true);
                    else
                        g.fill3DRect(r.x+r.width/2-1, r.y+2, 3, r.y+r.height-5, true);
                }
            }
        else {
            Rectangle r = getBounds();
            if (orientation==SplitterLayout.VERTICAL)
                g.fill3DRect(2, r.height/2-1, r.width-5, 3, true);
            else
                g.fill3DRect(r.width/2-1, 2, 3, r.height-5, true);
        }
    }

    void setOrientation(int o) {
        orientation = o;
    }

    public void swapOrientation() {
        setOrientation(getOrientation()==SplitterLayout.HORIZONTAL ? SplitterLayout.VERTICAL : SplitterLayout.HORIZONTAL);
    }

    /**
     * Called by AWT to update the image produced by the JSplitterBar
     */
    public void update(Graphics g) {
        paint(g);
    }

    public void setTooltip(String s) {
        // setToolTipText(s);
    }


}