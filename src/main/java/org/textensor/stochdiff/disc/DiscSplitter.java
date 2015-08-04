package org.textensor.stochdiff.disc;

import java.util.HashMap;
import java.util.HashSet;

import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.morph.VolumeLine;
import org.textensor.stochdiff.numeric.morph.VolumeSlice;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/*
 * Take a structure expressed as TreePoints each of which knows their
 * neighbors, and split it into radially and tangentially separated elements. The structure should already
 * have been segment-sliced so that here we just subdivide the segments
 * sideways.
 * The output will be the elements (positions, volumes etc) and coupling
 * constants.
 */
public class DiscSplitter {
    static final Logger log = LogManager.getLogger();

    TreePoint[] srcPoints;
    ArrayList<CurvedVolumeSlice> gridAL;
    HashSet<TreePoint> wkpHS;

    Resolution resolution;

    double[] surfaceLayers = new double[0];

    double maxAR = 3; // maximum aspect ratio for an element

    public DiscSplitter(TreePoint[] pts, double d, HashMap<String, Double> resHM, double[] sl, double mar) {
        srcPoints = pts;
        resolution = new Resolution(d, resHM);
        if (sl != null && sl.length > 0) {
            surfaceLayers = sl;
        }

        if (mar > 0.5) {
            maxAR = mar;
            log.info("set max aspect ratio {}", maxAR);
        }
    }


    public VolumeGrid buildGrid() {

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

        gridAL = new ArrayList<CurvedVolumeSlice>();
        CurvedVolumeSlice vg0 = null;
        wkpHS.remove(firstpt);

        recAdd(vg0, firstpt);

        VolumeGrid vgr = new VolumeGrid();

        vgr.importSmoothSlices(gridAL);
        return vgr;

    }





    private void recAdd(CurvedVolumeSlice pGrid, TreePoint tp) {
        String lbl = tp.getLabel();

        tp.partBranchOffset = 0.;


        for (TreePoint tpn : tp.getNeighbors()) {
            if (wkpHS.contains(tpn)) {
                wkpHS.remove(tpn);

                // if a terminal has a label, and the current point doesn't, then use it
                if (lbl == null && tpn.nnbr == 1 && tpn.getLabel() != null) {
                    lbl = tpn.getLabel();
                }

                CurvedVolumeSlice vg = null;
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
                    // normal case: make a new one or add a slice and connect
                    // it up with the centres aligned
                    if (pGrid == null) {
                        vg = baseGrid(tp, tpn, lbl);

                    } else {
                        // TODO - probably not what we want
                        // too much mumerical diffusion if boxes can have gradually changing
                        // sizes? restrict to a few dicrete multiples?
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




    public CurvedVolumeSlice baseGrid(TreePoint tpa, TreePoint tpb, String lbl) {

        String rgn = tpa.regionClassWith(tpb);
        double delta = resolution.getLocalDelta(tpa, tpb);

        CurvedVolumeSlice ret = new CurvedVolumeSlice(delta, tpa.getRadius(), tpb.getRadius());
        ret.discFill(tpa, tpb, lbl, rgn, surfaceLayers, maxAR);

        return ret;
    }












}
