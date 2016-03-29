package neurord.disc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

import neurord.numeric.morph.TreePoint;
import neurord.numeric.morph.VolumeGrid;
import neurord.numeric.morph.VolumeLine;
import neurord.numeric.morph.VolumeSlice;

/*
 * Take a structure expressed as TreePoints each of which knows thier
 * neighbors, and split it into cuboic boxes. The structure should already
 * have been segment-sliced so that here we just subdivide the segments
 * sideways.
 * The output will be the boxes (positions, volumes etc) and coupling
 * constants.
 */
public class DiscBoxer {

    TreePoint[] srcPoints;


    ArrayList<VolumeSlice> gridAL;
    HashSet<TreePoint> wkpHS;

    Resolution resolution;

    double[] surfaceLayers;

    public DiscBoxer(TreePoint[] pts, double[] sl) {
        srcPoints = pts;
        surfaceLayers = sl;
        if (sl != null && sl.length > 0)
            throw new RuntimeException("3d cuboid mesh doesn't handle surface layers yet");
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

        gridAL = new ArrayList<VolumeSlice>();
        VolumeSlice vg0 = null;
        wkpHS.remove(firstpt);

        recAdd(vg0, firstpt);

        VolumeGrid vgr = new VolumeGrid();
        vgr.importSlices(gridAL);
        return vgr;

    }





    private void recAdd(VolumeSlice pGrid, TreePoint tp) {
        String lbl = tp.getLabel();

        tp.partBranchOffset = 0.;


        for (TreePoint tpn : tp.getNeighbors()) {
            if (wkpHS.contains(tpn)) {
                wkpHS.remove(tpn);

                // if a terminal has a label, and the current point doesn't, then use it
                if (lbl == null && tpn.nnbr == 1 && tpn.getLabel() != null) {
                    lbl = tpn.getLabel();
                }

                VolumeSlice vg = null;
                if (tpn.subAreaPeer == tp) {
                    // nothing to do for now - put line in when we
                    // do the first child of tpn
                    // E.info("skipping pt with peer " + tpn);

                } else if (tp.subAreaPeer != null && tp.subAreaPeer == tp.parent) {
                    // E.info("first pt after branch " + tpn);
                    TreePoint par = tp.parent;
                    //  E.info("starting a sub-branch at " + tp + " - " + tpn + " " + pGrid);

                    vg = baseGrid(tp, tpn, lbl);
                    pGrid.subPlaneConnect(tp, tpn, vg, par.partBranchOffset);
                    par.partBranchOffset += 2 * tpn.getRadius();

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

    public VolumeSlice baseGrid(TreePoint tpa, TreePoint tpb, String lbl) {
        double r = 0.5 * (tpa.getRadius() + tpb.getRadius());

        String rgn = tpa.regionClassWith(tpb);
        double delta = resolution.getLocalDelta(tpa, tpb);


        VolumeSlice ret = new VolumeSlice(delta, r);
        ret.discFill(tpa, tpb, lbl, rgn);

        return ret;
    }
}
