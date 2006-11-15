package org.textensor.stochdiff.disc;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.stochdiff.numeric.morph.TreeWriter;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;

import java.io.File;

import java.util.HashMap;


public class TreeBoxDiscretizer {

    TreePoint[] srcPoints;


    public TreeBoxDiscretizer(TreePoint[] points) {
        srcPoints = points;
    }


    public VolumeGrid buildGrid(double d, HashMap<String, Double> resHM, int geom) {
        SegmentSlicer ss = new SegmentSlicer(srcPoints);

        TreePoint[] slicedPoints = ss.getFixedWidthSlices(d, resHM);

        E.info("segment divisions yields " + slicedPoints.length + " points");
        TreeWriter tw = new TreeWriter(slicedPoints);
        tw.writeSWC(new File("discretized-tree.swc"));

        VolumeGrid vgrid = null;

        if (geom == VolumeGrid.GEOM_2D) {
            LineBoxer lb = new LineBoxer(slicedPoints);
            vgrid = lb.buildGrid(d);

        } else if (geom == VolumeGrid.GEOM_3D) {
            DiscBoxer db = new DiscBoxer(slicedPoints);
            vgrid = db.buildGrid(d);

        } else {
            E.error("unrecognized geometry");
        }
        return vgrid;
    }


}
