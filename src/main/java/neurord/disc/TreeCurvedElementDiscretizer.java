package neurord.disc;

import neurord.numeric.morph.TreePoint;
import neurord.numeric.morph.TreeWriter;
import neurord.numeric.morph.VolumeGrid;

import java.io.File;

import java.util.HashMap;


public class TreeCurvedElementDiscretizer {

    TreePoint[] srcPoints;


    public TreeCurvedElementDiscretizer(TreePoint[] points) {
        srcPoints = points;
    }


    public VolumeGrid buildGrid(double d, HashMap<String, Double> resHM, double[] surfaceLayers, double mar) {

        TreePoint base = srcPoints[0];
        TreeUtil.parentizeFrom(base, srcPoints);
        TreeUtil.orientAC(base, srcPoints);

        SegmentSlicer ss = new SegmentSlicer(srcPoints);


        TreePoint[] slicedPoints = ss.getFixedWidthSlices(d, resHM);

        TreeWriter tw = new TreeWriter(slicedPoints);
        tw.writeSWC(new File("discretized-tree.swc"));

        VolumeGrid vgrid = null;

        DiscSplitter dsplit = new DiscSplitter(slicedPoints, d, resHM, surfaceLayers, mar);
        vgrid = dsplit.buildGrid();

        return vgrid;
    }


}
