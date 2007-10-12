package org.textensor.stochdiff.disc;

import java.util.HashMap;
import java.util.HashSet;

import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.morph.VolumeLine;
import org.textensor.stochdiff.numeric.morph.VolumeSlice;

import java.util.ArrayList;

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



    public DiscBoxer(TreePoint[] pts) {
        srcPoints = pts;
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
        for (TreePoint tpn : tp.getNeighbors()) {
            if (wkpHS.contains(tpn)) {
                wkpHS.remove(tpn);

                // if a terminal has a label, and the current point doesn't, then use it
                if (lbl == null && tpn.nnbr == 1 && tpn.getLabel() != null) {
                    lbl = tpn.getLabel();
                }

                VolumeSlice vg = nextVolumeSlice(pGrid, tp, tpn, lbl);
                lbl = null;
                gridAL.add(vg);
                recAdd(vg, tpn);
            }
        }
    }




    public VolumeSlice nextVolumeSlice(VolumeSlice parentGrid,
                                       TreePoint tpa, TreePoint tpb, String lbl) {

        VolumeSlice ret = null;
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


    public VolumeSlice baseGrid(TreePoint tpa, TreePoint tpb, String lbl) {
        double r = 0.5 * (tpa.getRadius() + tpb.getRadius());

        String rgn = tpa.regionClassWith(tpb);
        double delta = resolution.getLocalDelta(tpa, tpb);


        VolumeSlice ret = new VolumeSlice(delta, 2. * r);
        ret.discFill(tpa, tpb, lbl, rgn);

        return ret;
    }












}
