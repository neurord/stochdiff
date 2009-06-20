package org.catacomb.druid.swing;


import org.catacomb.druid.event.ClosureListener;
import org.catacomb.report.E;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.WindowConstants;


import java.awt.Dimension;


public class DFrame extends JFrame implements WindowListener, ComponentListener {

    static final long serialVersionUID = 1001;

    String name = "anon";

    int px, py, pw, ph;

    int desx;
    int desy;
    int[] pidat;


    final static int UNKNOWN = 0;
    final static int YES = 1;
    final static int NO = 2;

    static int stateSettable = 0;

    static int FRAME_NORMAL = 0;
    static Method setStateMethod = null;

    ClosureListener clisten;


    public DFrame() {
        this("anon");
    }


    public DFrame(String s) {
        super();
        name = s;
        setTitle(s);
        // addComponentListener (this);
        addWindowListener(this);
        addComponentListener(this);

        getContentPane().addComponentListener(this);

        setBg(LAF.getBackgroundColor());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
    }


    public void setClosureListener(ClosureListener cl) {
        clisten = cl;
    }


    public void setBg(Color c) {
        setBackground(c);
    }


    public int[] getIntArraySize() {
        int[] wh = new int[2];
        wh[0] = getWidth();
        wh[1] = getHeight();
        return wh;
    }


    public void setName(String s) {
        name = s;
    }


    public String getName() {
        return name;
    }


    public void setTitle(String s) {
        super.setTitle(s);
    }


    public void myprint(String s) {
        System.out.println(" cframe wcev " + name + " " + s);
    }


    /*
     * public Point getLocationOnScreen() { return
     * getRootPane().getLocationOnScreen(); }
     */


    /*
     * public void setScreenLocation(int x, int y) { desx = x; desy = y;
     * Container c = getParent(); if (c == null) { setLocation(x, y); } else {
     * System.out.println("DEBUG? - non null parent of frame " + c); Point p =
     * c.getLocationOnScreen(); setLocation(x-p.x, y-p.y); } }
     */



    // this is a workaround for the random way 1.3 brings up windows
    // iconized. The setState method deosn't exist in 1.1, so we
    // use reflection to call it, if present
    void checkStateSettable() {
        setStateMethod = null;
        FRAME_NORMAL = -999;
        try {
            Method[] ms = getClass().getMethods();
            for (int i = 0; i < ms.length; i++) {
                if (ms[i].getName().equals("setState")) {
                    setStateMethod = ms[i];
                    break;
                }
            }

            Field[] fs = getClass().getFields();
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].getName().equals("NORMAL")) {
                    FRAME_NORMAL = fs[i].getInt(this);
                }
            }

        } catch (Exception e) {
            System.out.println("couldnt do frame stuf " + e);
        }
        stateSettable = ((setStateMethod != null && FRAME_NORMAL != -999) ? YES : NO);
    }


    void applySetState() {
        try {
            Integer ii = new Integer(FRAME_NORMAL);
            Object[] args = { ii };
            setStateMethod.invoke(this, args);
        } catch (Exception e) {
            System.out.println("caught exception when trying to set state " + e);
        }
    }



    /*
     * public void setVisible(boolean b) { if (stateSettable == UNKNOWN)
     * checkStateSettable(); if (stateSettable == YES) applySetState();
     * super.setVisible(b); }
     */



    public void componentHidden(ComponentEvent e) {
    }


    public void componentMoved(ComponentEvent e) {
        dce(e);
    }


    public void componentResized(ComponentEvent e) {
        dce(e);
    }


    public void componentShown(ComponentEvent e) {
    }; // dce(e); }

    @SuppressWarnings("unused")
    private void dce(ComponentEvent e) {
        if (isVisible()) {
            Rectangle r = getBounds();
            if (Math.abs(r.x - px) > 10 || Math.abs(r.y - py) > 10 || Math.abs(r.width - pw) > 10
                    || Math.abs(r.height - ph) > 10) {
                px = r.x;
                py = r.y;
                pw = r.width;
                ph = r.height;

            }
        }
    }


    public void windowActivated(WindowEvent e) {
    }


    public void windowDeactivated(WindowEvent e) {
    }


    public void windowClosed(WindowEvent e) {
        if (clisten != null) {
            clisten.closed();
        }
    }


    public void windowClosing(WindowEvent e) {
        if (clisten != null) {
            clisten.requestClose();
        }
    }



    public void windowDeiconified(WindowEvent e) {
    }


    public void windowIconified(WindowEvent e) {
    }


    public void windowOpened(WindowEvent e) {
    }



    public void newState(String s) {
        if (s.equals("iconified")) {
            setVisible(false);

        } else if (s.equals("deiconified")) {
            setVisible(false);

        } else if (s.equals("closed")) {
            setVisible(false);

        } else if (s.startsWith("moved")) {
            StringTokenizer st = new StringTokenizer(s, ",");
            st.nextToken();
            int ix = Integer.parseInt(st.nextToken());
            int iy = Integer.parseInt(st.nextToken());
            int iw = Integer.parseInt(st.nextToken());
            int ih = Integer.parseInt(st.nextToken());
            setBounds(new Rectangle(ix, iy, iw, ih));

        } else {
            E.error(" - unknown stat change in frame " + s);

        }
    }


    public void setPreferredSize(int w, int h) {
        setPreferredSize(new Dimension(w, h));
    }

}
