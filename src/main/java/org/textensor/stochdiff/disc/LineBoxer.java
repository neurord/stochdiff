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
    static final Logger log = LogManager.getLogger();

    final TreePoint[] srcPoints;
    final double[] surfaceLayers;
    final double depth;

    Resolution resolution;

    public LineBoxer(TreePoint[] srcPoints, double[] surfaceLayers, double depth) {
        this.srcPoints = srcPoints;
        this.surfaceLayers = surfaceLayers;
        this.depth = depth;
    }

    public VolumeGrid buildGrid(double d, HashMap<String, Double> resHM) {
        this.resolution = new Resolution(d, resHM);

        TreePoint firstpt = null;
        // put them all in a set - take them out when they've been done;
        final HashSet<TreePoint> working_set = new HashSet<>();

        for (TreePoint tp : srcPoints)
            // need one that has only one neighbor as the start point;
            if (firstpt == null && tp.isEndPoint()) {
                firstpt = tp;
                log.debug("Using {} as the starting point", firstpt);
            } else {
                working_set.add(tp);
                log.debug("Will process {} later", tp);
            }

        TreeUtil.parentizeFrom(firstpt, srcPoints);

        final ArrayList<VolumeLine> volume_lines = new ArrayList<VolumeLine>();
        recAdd(working_set, volume_lines, null, firstpt);

        VolumeGrid vgr = new VolumeGrid();
        vgr.importLines(volume_lines);
        return vgr;
    }

    private void recAdd(HashSet<TreePoint> working_set, ArrayList<VolumeLine> volume_lines,
                        VolumeLine pGrid, TreePoint tp) {
        String lbl = tp.getLabel();

        tp.partBranchOffset = 0.;

        log.debug("Processing {}", tp);

        for (TreePoint tpn : tp.getNeighbors())
            if (working_set.contains(tpn)) {
                working_set.remove(tpn);

                log.debug("looking at neighbour {}", tpn);

                // if the current point doesn't have a label, try to use one from the terminal
                if (lbl == null && tpn.nnbr == 1)
                    lbl = tpn.getLabel();

                VolumeLine vg = null;
                if (tpn.subAreaPeer == tp) {
                    // nothing to do for now - put line in when we
                    // do the first child of tpn
                    log.debug("{}: skipping pt with peer", tpn);

                } else if (tp.subAreaPeer != null && tp.subAreaPeer == tp.parent) {
                    log.debug("{}: first pt after branch", tpn);
                    TreePoint par = tp.parent;
                    log.info("starting a sub-branch at {} - {} {}", tp, tpn, pGrid);

                    vg = baseGrid(tp, tpn, lbl);
                    pGrid.subPlaneConnect(tp, tpn, vg, par.partBranchOffset);
                    par.partBranchOffset += 2 * tpn.r;

                } else {
                    log.debug("Creating new grid between neighbouring points");
                    vg = baseGrid(tp, tpn, lbl);
                    if (pGrid != null) {
                        // TODO - probably not what we want
                        // too much numerical diffusion if boxes can have gradually changing
                        // sizes? restrict to a few discrete multiples?
                        log.debug("Attaching with planeConnect()");
                        pGrid.planeConnect(vg);
                    }
                }

                lbl = null; // only use it once
                if (vg != null) {
                    volume_lines.add(vg);
                    recAdd(working_set, volume_lines, vg, tpn);
                } else {
                    // skipped the point that is the start of a new segment
                    // of different radius
                    recAdd(working_set, volume_lines, pGrid, tpn);
                }
            } else
                log.debug("neighbour {} already removed from the working set", tpn);
    }

    public VolumeLine baseGrid(TreePoint tpa, TreePoint tpb, String lbl) {
        VolumeLine ret = null;

        double delta = resolution.getLocalDelta(tpa, tpb);
        String rgn = tpa.regionClassWith(tpb);

        return baseHardGrid(tpa, tpb, lbl, rgn, delta);
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
}
