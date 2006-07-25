package org.textensor.stochdiff.disc;

import java.util.HashSet;

import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.morph.VolumeLine;


/*
 * Take a structure expressed as TreePoints each of which knows thier
 * neighbors, and split it into square boxes. The structure should already
 * have been segment-sliced so that here we just subdivide the segments
 * sideways.
 * The output will be the boxes (positions, volumes etc) and coupling
 * constants.
 *
 *  ***This produces a 2D grid, mainly for visualization and testing***
 *  Use the DiscBoxer for a full 3D grid.
 */



public class LineBoxer {

    TreePoint[] srcPoints;


    HashSet<VolumeLine> gridHS;
    HashSet<TreePoint> wkpHS;

    double delta;


    public LineBoxer(TreePoint[] pts) {
        srcPoints = pts;
    }


    public VolumeGrid buildGrid(double d) {
        delta = d;

        TreePoint firstpt = null;
        // put them all in a set - take them out when they've been done;
        wkpHS = new HashSet<TreePoint>();
        for (TreePoint tp : srcPoints) {
            wkpHS.add(tp);
            // need one that has only one neighbor as the start point;
            if (firstpt == null && tp.isEndPoint()) {
                firstpt = tp;
            }
        }

        gridHS = new HashSet<VolumeLine>();
        VolumeLine vg0 = null;
        wkpHS.remove(firstpt);

        recAdd(vg0, firstpt);

        VolumeGrid vgr = new VolumeGrid();
        vgr.importLines(gridHS);
        return vgr;

    }





    private void recAdd(VolumeLine pGrid, TreePoint tp) {
        String lbl = tp.getLabel();
        String rgn = tp.getRegion();
        // POSERR - which points does the region label apply to?

        for (TreePoint tpn : tp.getNeighbors()) {
            if (wkpHS.contains(tpn)) {
                wkpHS.remove(tpn);
                VolumeLine vg = nextVolumeLine(pGrid, tp, tpn, lbl, rgn);
                lbl = null; // unly use it once
                gridHS.add(vg);
                recAdd(vg, tpn);
            }
        }
    }




    public VolumeLine nextVolumeLine(VolumeLine parentGrid,
                                     TreePoint tpa, TreePoint tpb, String lbl, String rgn) {

        VolumeLine ret = null;
        if (parentGrid == null) {
            ret = baseGrid(tpa, tpb, lbl, rgn);

        } else {
            // TODO - probably not what we want
            // too much mumerical diffusion if boxes can have gradually changing
            // sizes? restrict to a few dicrete multiples?
            ret = baseGrid(tpa, tpb, lbl, rgn);
            parentGrid.planeConnect(ret);
        }
        return ret;
    }


    public VolumeLine baseGrid(TreePoint tpa, TreePoint tpb, String lbl,
                               String rgn) {
        VolumeLine ret = null;
        if (false) {
            ret = baseSoftGrid(tpa, tpb, lbl, rgn);
        } else {
            ret = baseHardGrid(tpa, tpb, lbl, rgn);
        }
        return ret;
    }



    public VolumeLine baseHardGrid(TreePoint tpa, TreePoint tpb, String lbl,
                                   String rgn) {
        // odd number of cells, fixed size
        double r = 0.5 * (tpa.getRadius() + tpb.getRadius());

        // number of boxes across the diameter;
        int nd = 1 + 2 * ((int)(r / delta));

        VolumeLine ret = new VolumeLine(nd, nd * delta);
        ret.lineFill(tpa, tpb, lbl, rgn);

        return ret;
    }


    public VolumeLine baseSoftGrid(TreePoint tpa, TreePoint tpb, String lbl,
                                   String rgn) {
        // allows the cells to grow or shrink a little to fill the line;
        double r = 0.5 * (tpa.getRadius() + tpb.getRadius());

        // number of boxes across the diameter;
        int nd = (int)(2 * r / delta + 0.5);
        if (nd < 1) {
            nd = 1;
        }

        VolumeLine ret = new VolumeLine(nd, 2. * r);
        ret.lineFill(tpa, tpb, lbl, rgn);

        return ret;
    }

}
