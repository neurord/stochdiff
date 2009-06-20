package org.catacomb.druid.swing;

import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.MouseActor;
import org.catacomb.interlish.structure.MouseSource;
import org.catacomb.report.E;
import org.catacomb.util.ColorUtil;


import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;


public class DPanel extends JPanel implements DComponent, MouseSource {

    static final long serialVersionUID = 1001;

    private Border lineborder;

    private Font borderFont;
    private Color borderColor;

    public final static int PLAINFONT = 1;
    public final static int BOLDFONT = 2;


    public void setMouseActor(MouseActor ma) {
        addMouseListener(new DMouseRelay(ma));
    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }

    public void setBg(Color c) {
        setBackground(c);
        if (c instanceof javax.swing.plaf.ColorUIResource) {
            E.warning("default UI bg color being applied - should be overridden: ");
            (new Exception()).printStackTrace();
        }
    }


    public void setFg(Color c) {
        setForeground(c);
        if (c instanceof javax.swing.plaf.ColorUIResource) {
            E.warning("default UI fg color being applied - should be overridden: ");
            (new Exception()).printStackTrace();
        }
    }



    public int[] getCenterLocationOnScreen() {
        Point p = getLocationOnScreen();
        Dimension d = getSize();
        int[] xy = new int[2];
        xy[0] = p.x + d.width / 2;
        xy[1] = p.y + d.height / 2;
        return xy;
    }


    public int[] getXYLocation() {
        return getCenterLocationOnScreen();
    }

    public void localSetBorder(Border b) {
        if (getBorder() instanceof TitledBorder) {
            E.error("replacing titled border with " + b);
        }
        super.setBorder(b);
    }


    public void setEtchedBorder(Color col) {
        localSetBorder(BorderUtil.makeEtchedBorder(col));
    }


    public void setEtchedUpBorder(Color col) {
        localSetBorder(BorderUtil.makeEtchedUpBorder(col));
    }


    public void addEtchedBorder(Color col) {
        Border bb = BorderUtil.makeEtchedBorder(col);
        addBorder(bb);
    }

    public void addSunkenBorder(Color col) {

        Color cbr = ColorUtil.brighter(col);
        Color cdk = ColorUtil.slightlyDarker(col);

        Border bb = BorderFactory.createBevelBorder(BevelBorder.LOWERED, col, cbr, cdk, col);
        addBorder(bb);
    }


    public void clearBorder() {
        if (getBorder() != null) {
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
    }


    public void addBorder(int l, int r, int t, int b) {
        if (l == 0 && r == 0 && t == 0 && b == 0) {
            E.warning("DPanel add border called with zero width " + this);
        } else {
            Border border = BorderFactory.createEmptyBorder(t, l, b, r);
            addBorder(border);
        }
    }

    public void setEmptyBorder(int l, int r, int t, int b) {
        Border border = BorderFactory.createEmptyBorder(t, l, b, r);
        localSetBorder(border);

    }




    private void addBorder(Border bb) {
        Border bsf = getBorder();
        if (bsf == null) {
            localSetBorder(bb);
        } else {
            CompoundBorder cbd = new CompoundBorder(bb, bsf);
            localSetBorder(cbd);
        }
    }





    public void setSunkenBorder(Color col) {
        Color cbr = col.brighter();
        Color cdk = col.darker();

        localSetBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, col, cbr, cdk, col));
    }



    public void setFlowLayout() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    }


    public void setSingle() {
        setLayout(new GridLayout(1, 1, 0, 0));
    }


    public void addSingle(Object obj) {
        setSingle();
        add((JComponent)obj);
    }


    public void setFlowLeft(int h, int v) {
        setLayout(new FlowLayout(FlowLayout.LEFT, h, v));
    }


    public void setFlowRight(int h, int v) {
        setLayout(new FlowLayout(FlowLayout.RIGHT, h, v));
    }

    public void setFlowCenter(int h, int v) {
        setLayout(new FlowLayout(FlowLayout.CENTER, h, v));
    }


    public void setBorderLayout() {
        setLayout(new BorderLayout(0, 0));
    }


    public void setBorderLayout(int h, int v) {
        setLayout(new BorderLayout(h, v));
    }



    public void addTitledBorder(String s, Color c) {
        if (lineborder == null) {
//         lineborder = BorderFactory.createLineBorder(new Color(210, 210, 210));
            lineborder = BorderFactory.createLineBorder(c);
        }

        if (borderFont == null) {
            borderFont = new Font("sansserif", Font.BOLD, 12);
            borderColor = new Color(90, 90, 90);
        }

        localSetBorder(BorderFactory.createTitledBorder(lineborder, s,
                       TitledBorder.LEADING, TitledBorder.TOP, borderFont, borderColor));
    }


    public void addTitledBorder(String s, int ifont, int txtcol, int linecol) {
        lineborder = BorderFactory.createLineBorder(new Color(linecol));

        Font f = null;
        if (ifont == PLAINFONT) {
            f = new Font("sansserif", Font.PLAIN, 12);
        } else {
            f = new Font("sansserif", Font.BOLD, 12);
        }

        Color tc = new Color(txtcol);
        Border b = BorderFactory.createTitledBorder(lineborder, s, TitledBorder.CENTER, TitledBorder.TOP, f, tc);
        addBorder(b);
    }


    public void setPreferredSize(int w, int h) {
        setPreferredSize(new Dimension(w, h));
    }


    public int[] getXYLocationOnScreen() {
        int[] ixy = {400, 400};
        if (isShowing()) {
            Point p = getLocationOnScreen();
            ixy[0] = (int)(p.getX());
            ixy[1] = (int)(p.getY());
        }
        return ixy;
    }


    public void addDComponent(DComponent obj) {
        add((JComponent)obj);
    }

    public void addDComponent(DComponent obj, Object constraints) {
        add((JComponent)obj, constraints);
    }


    public void removeDComponent(DComponent obj) {
        remove((JComponent)obj);
    }


    public void setGridLayout(int nr, int nc, int dx, int dy) {
        setLayout(new GridLayout(nr, nc, dx, dy));
    }

}
