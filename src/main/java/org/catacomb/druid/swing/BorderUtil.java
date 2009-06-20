package org.catacomb.druid.swing;

import org.catacomb.util.ColorUtil;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;


public class BorderUtil {


    public final static int NONE = 0;
    public final static int ETCHED_DOWN = 1;
    public final static int ETCHED_UP = 2;
    public final static int RAISED = 2;
    public final static int CURRENT = 3;



    public static Border makeEtchedBorder(Color c) {
        return makeBorder(ETCHED_DOWN, c);
    }

    public static Border makeEtchedUpBorder(Color c) {
        return makeBorder(ETCHED_UP, c);
    }


    public static Border makeEmptyBorder(int p) {
        return BorderFactory.createEmptyBorder(p, p, p, p);
    }

    public static Border makeEmptyBorder(int pleft, int pright, int ptop, int pbot) {
        return BorderFactory.createEmptyBorder(ptop, pleft, pbot, pright);
    }


    public static Border makeSunkenBorder(Color cb, Color co) {
        Color cbr = ColorUtil.brighter(co);
        Color cdk = ColorUtil.darker(cb);
        Color cdk2 = ColorUtil.darker(cdk);
        return BorderFactory.createBevelBorder(BevelBorder.LOWERED, cbr, cb, cdk2, cdk);
    }


    public static Border makeRaisedBorder(Color cb, Color co) {
        Color cbr = ColorUtil.brighter(co);
        Color cdk = ColorUtil.darker(cb);
        Color cdk2 = ColorUtil.darker(cdk);
        return BorderFactory.createBevelBorder(BevelBorder.RAISED, cbr, cb, cdk2, cdk);
    }



    public static Border makeSlightlySunkenBorder(Color col) {
        Color cbr = ColorUtil.slightlyBrighter(col);
        Color cdk = ColorUtil.slightlyDarker(col);
        return BorderFactory.createBevelBorder(BevelBorder.LOWERED, col, cbr, cdk, col);
    }


    public static Border makeBorder(int type, Color c) {
        Color cbr = ColorUtil.brighter(c);
        Color cdk = ColorUtil.darker(c);

        Border ret = null;
        if (type == ETCHED_DOWN) {
            //	 ret = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            ret = BorderFactory.createEtchedBorder(cbr, cdk);

        } else if (type == ETCHED_UP) {
            //	 ret = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
            ret = BorderFactory.createEtchedBorder(cdk, cbr);
        } else {
            ret = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        }
        return ret;
    }

    public static Border makeCompoundBorder(Border bin, Border bout) {
        return BorderFactory.createCompoundBorder(bout, bin);
    }

    public static Border makeZeroBorder() {
        return makeEmptyBorder(0);
    }






}
