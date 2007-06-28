package org.textensor.stochdiff.disc;

import java.util.HashSet;

import java.util.HashMap;

import java.util.ArrayList;

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


    ArrayList<VolumeLine> gridAL;
    HashSet<TreePoint> wkpHS;


    double depth;

    Resolution resolution;


    public LineBoxer(TreePoint[] pts, double d2d) {
        srcPoints = pts;
        depth = d2d;
    }


    public VolumeGrid buildGrid(double d, HashMap<String, Double> resHM) {
        resolution = new Resolution(d, resHM);

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

        gridAL = new ArrayList<VolumeLine>();
        VolumeLine vg0 = null;
        wkpHS.remove(firstpt);

        recAdd(vg0, firstpt);

        VolumeGrid vgr = new VolumeGrid();
        vgr.importLines(gridAL);
        return vgr;

    }





    private void recAdd(VolumeLine pGrid, TreePoint tp) {
        String lbl = tp.getLabel();

        for (TreePoint tpn : tp.getNeighbors()) {
            if (wkpHS.contains(tpn)) {
                wkpHS.remove(tpn);

                // if a terminal has a label, and the current point doesn't, then use it
                if (lbl == null && tpn.nnbr == 1 && tpn.getLabel() != null) {
                    lbl = tpn.getLabel();
                }

                VolumeLine vg = nextVolumeLine(pGrid, tp, tpn, lbl);
                lbl = null; // unly use it once
                gridAL.add(vg);
                recAdd(vg, tpn);
            }
        }
    }




    public VolumeLine nextVolumeLine(VolumeLine parentGrid,
                                     TreePoint tpa, TreePoint tpb, String lbl) {

        VolumeLine ret = null;
        if (parentGrid == null) {
            ret = baseGrid(tpa, tpb, lbl);

        } else {
            // TODO - probably not what we want
            // too much mumerical diffusion if boxes can have gradually changing
            // sizes? restrict to a few dicrete multiples?
            ret = baseGrid(tpa, tpb, lbl);
            parentGrid.planeConnect(ret);
        }
        return ret;
    }


    public VolumeLine baseGrid(TreePoint tpa, TreePoint tpb, String lbl) {
        VolumeLine ret = null;

        double delta = resolution.getLocalDelta(tpa, tpb);
        String rgn = tpa.regionClassWith(tpb);

        if (false) {
            ret = baseSoftGrid(tpa, tpb, lbl, rgn, delta);
        } else {
            ret = baseHardGrid(tpa, tpb, lbl, rgn, delta);
        }
        return ret;
    }



    public VolumeLine baseHardGrid(TreePoint tpa, TreePoint tpb, String lbl,
                                   String rgn, double delta) {
        // odd number of cells, fixed size
        double r = 0.5 * (tpa.getRadius() + tpb.getRadius());

        // number of boxes across the diameter;
        int nd = 1 + 2 * ((int)(r / delta));

        VolumeLine ret = new VolumeLine(nd, nd * delta, depth);
        ret.lineFill(tpa, tpb, lbl, rgn);

        return ret;
    }


    public VolumeLine baseSoftGrid(TreePoint tpa, TreePoint tpb, String lbl,
                                   String rgn, double delta) {
        // allows the cells to grow or shrink a little to fill the line;
        double r = 0.5 * (tpa.getRadius() + tpb.getRadius());



        // number of boxes across the diameter;
        int nd = (int)(2 * r / delta + 0.5);
        if (nd < 1) {
            nd = 1;
        }

        VolumeLine ret = new VolumeLine(nd, 2. * r, depth);
        ret.lineFill(tpa, tpb, lbl, rgn);

        return ret;
    }

}
