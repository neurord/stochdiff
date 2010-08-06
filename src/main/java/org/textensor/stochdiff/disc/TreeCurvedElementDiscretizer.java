package org.textensor.stochdiff.disc;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.stochdiff.numeric.morph.TreeWriter;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;

import java.io.File;

import java.util.HashMap;


public class TreeCurvedElementDiscretizer {

    TreePoint[] srcPoints;


    public TreeCurvedElementDiscretizer(TreePoint[] points) {
        srcPoints = points;
    }


    public VolumeGrid buildGrid(double d, HashMap<String, Double> resHM, double sl, double mar) {

        TreePoint base = srcPoints[0];
        TreeUtil.parentizeFrom(base, srcPoints);
        TreeUtil.orientAC(base, srcPoints);

        SegmentSlicer ss = new SegmentSlicer(srcPoints);


        TreePoint[] slicedPoints = ss.getFixedWidthSlices(d, resHM);

        TreeWriter tw = new TreeWriter(slicedPoints);
        tw.writeSWC(new File("discretized-tree.swc"));

        VolumeGrid vgrid = null;

        DiscSplitter db = new DiscSplitter(slicedPoints, d, resHM, sl, mar);
        vgrid = db.buildGrid();

        return vgrid;
    }


}
