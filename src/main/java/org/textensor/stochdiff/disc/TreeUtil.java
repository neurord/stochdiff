package org.textensor.stochdiff.disc;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.morph.TreePoint;

public class TreeUtil {




    public static void parentizeFrom(TreePoint base, TreePoint[] srcPoints) {
        for (TreePoint tp : srcPoints) {
            tp.iwork = 1;
        }
        Queue<TreePoint> q = new ConcurrentLinkedQueue<TreePoint>();
        q.add(base);
        while (!q.isEmpty()) {
            TreePoint tp = q.remove();
            for (int i = 0; i < tp.nnbr; i++) {
                TreePoint tpn = tp.nbr[i];
                if (tpn.iwork == 1) {
                    tpn.iwork = 0;
                    tpn.parent = tp;
                    q.add(tpn);
                }
            }
        }
    }

    public static void orientAC(TreePoint base, TreePoint[] srcPoints) {
        // reorder neighbours if necessary so   parent, neighbor 1, neighbor2
        // go anticlockwise
        for (TreePoint tp : srcPoints) {
            tp.iwork = 1;
        }
        Queue<TreePoint> q = new ConcurrentLinkedQueue<TreePoint>();
        q.add(base);
        while (!q.isEmpty()) {
            TreePoint tp = q.remove();
            tp.iwork = 0;

            // make parent first neighbor
            for (int i = 1; i < tp.nnbr; i++) {
                if (tp.nbr[i] == tp.parent) {
                    tp.nbr[i] = tp.nbr[0];
                    tp.nbr[0] = tp.parent;
                    break;
                }
            }
            if (tp.nnbr >= 3) {
                // may need to reorder children
                orientOneAC(tp);
            }
            for (int i = 0; i < tp.nnbr; i++) {
                TreePoint tpn = tp.nbr[i];
                if (tpn.iwork == 1) {
                    tpn.iwork = 0;
                    q.add(tpn);
                }
            }

        }
    }


    private static void orientOneAC(TreePoint tp) {
        double[][] ds = new double[tp.nnbr][2];
        TreePoint wk = tp.parent;
        if (tp.distanceTo(wk) < 0.1) {
            wk = wk.parent;
        }
        ds[0][0] = wk.getX() - tp.getX();
        ds[0][1] = wk.getY() - tp.getY();

        for (int i = 1; i < tp.nnbr; i++) {
            wk = tp.nbr[i];
            if (tp.distanceTo(wk) < 0.1) {
                if (wk.nnbr >= 2) {
                    wk = wk.nbr[1];
                } else {
                    E.error("problem orienting children of " + tp);
                }
            }
            ds[i][0] = wk.getX() - tp.getX();
            ds[i][1] = wk.getY() - tp.getY();
        }

        double[] ang = new double[tp.nnbr];
        for (int i = 0; i < tp.nnbr; i++) {
            ang[i] = Math.atan2(ds[i][1], ds[i][0]);
        }
        for (int i = 1; i < tp.nnbr; i++) {
            ang[i] = ((ang[i] - ang[0]) + 4 * Math.PI) % (2 * Math.PI);
        }
        ang[0] = 0;

        // E.info("angles rel to parent: " + ang[1] + " " + ang[2]);

        if (tp.nnbr == 3) {
            if (ang[1] > ang[2]) {
                // E.info("switching children");
                TreePoint dum = tp.nbr[1];
                tp.nbr[1] = tp.nbr[2];
                tp.nbr[2] = dum;
            }

        } else {
            E.missing("can't handle points with more than three neighbors yet");
        }

    }


}
