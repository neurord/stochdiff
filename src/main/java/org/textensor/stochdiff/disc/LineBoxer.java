package org.textensor.stochdiff.disc;

import java.util.HashSet;
import java.util.Queue;

import java.util.HashMap;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.morph.VolumeLine;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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

// TODO - make a subclass of Boxer, share with VolumeBoxer, pull
// various things up
public class LineBoxer {
    static final Logger log = LogManager.getLogger(LineBoxer.class);

    TreePoint[] srcPoints;


    ArrayList<VolumeLine> gridAL;
    HashSet<TreePoint> wkpHS;

    double[] surfaceLayers;

    double depth;

    Resolution resolution;


    public LineBoxer(TreePoint[] pts, double[] sl, double d2d) {
        srcPoints = pts;
        surfaceLayers = sl;
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

        TreeUtil.parentizeFrom(firstpt, srcPoints);


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

        tp.partBranchOffset = 0.;

        //  E.info("processing " + tp);

        for (TreePoint tpn : tp.getNeighbors()) {
            if (wkpHS.contains(tpn)) {
                wkpHS.remove(tpn);

                // if a terminal has a label, and the current point doesn't, then use it
                if (lbl == null && tpn.nnbr == 1 && tpn.getLabel() != null) {
                    lbl = tpn.getLabel();
                }

                VolumeLine vg = null;
                if (tpn.subAreaPeer == tp) {
                    // nothing to do for now - put line in when we
                    // do the first child of tpn
                    // E.info("skipping pt with peer " + tpn);

                } else if (tp.subAreaPeer != null && tp.subAreaPeer == tp.parent) {
                    // E.info("first pt after branch " + tpn);
                    TreePoint par = tp.parent;
                    log.info("starting a sub-branch at {} - {} {}", tp, tpn, pGrid);

                    vg = baseGrid(tp, tpn, lbl);
                    pGrid.subPlaneConnect(tp, tpn, vg, par.partBranchOffset);
                    par.partBranchOffset += 2 * tpn.r;

                } else {
                    if (pGrid == null) {
                        vg = baseGrid(tp, tpn, lbl);

                    } else {
                        // TODO - probably not what we want
                        // too much numerical diffusion if boxes can have gradually changing
                        // sizes? restrict to a few discrete multiples?
                        vg = baseGrid(tp, tpn, lbl);
                        pGrid.planeConnect(vg);
                    }

                }

                lbl = null; // only use it once
                if (vg != null) {
                    gridAL.add(vg);
                    recAdd(vg, tpn);
                } else {
                    // skipped the point that is the start of a new segment
                    // of different radius
                    recAdd(pGrid, tpn);
                }
            }
        }
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
        double r = 0.5 * (tpa.getRadius() + tpb.getRadius()) - 1.e-7;
        // subtract 1.e-7 in case user provides exact multiples.

        double dsl = 0;
        int nsl = 0;

        if (surfaceLayers != null && surfaceLayers.length > 0) {
            while (nsl < surfaceLayers.length && dsl + surfaceLayers[nsl] < r) {
                dsl += surfaceLayers[nsl];
                nsl += 1;
            }
        }
        double dleft = 2 * (r - dsl);


        // number of regular boxes across the inner part once nsl put on each end
        // RCC - old form was lacking a factor of 2 to divide dleft to get the radius:
        // int nreg = 1 + 2 * ((int)((dleft) / delta));

        int nreg = 1 + 2 * ((int)((dleft/2) / delta));



        double dtot = 2 * dsl + nreg * delta;
        VolumeLine ret = new VolumeLine(nsl, nreg, surfaceLayers, dtot, depth);
        ret.lineFill(tpa, tpb, lbl, rgn);

        return ret;
    }


    public VolumeLine baseSoftGrid(TreePoint tpa, TreePoint tpb, String lbl,
                                   String rgn, double delta) {
        if (surfaceLayers != null && surfaceLayers.length > 0)
            log.warn("Surface layers are incompatible with soft grid - ignoring");

        // allows the cells to grow or shrink a little to fill the line;
        double r = 0.5 * (tpa.getRadius() + tpb.getRadius());

        // number of boxes across the diameter;
        int nd = (int)(2 * r / delta + 0.5);
        if (nd < 1) {
            nd = 1;
        }

        VolumeLine ret = new VolumeLine(0, nd, null, 2. * r, depth);
        ret.lineFill(tpa, tpb, lbl, rgn);

        return ret;
    }
}
