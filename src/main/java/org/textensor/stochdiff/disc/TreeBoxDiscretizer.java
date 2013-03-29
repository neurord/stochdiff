package org.textensor.stochdiff.disc;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.stochdiff.numeric.morph.TreeWriter;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.morph.VolumeGrid.geometry_t;

import java.io.File;

import java.util.HashMap;


public class TreeBoxDiscretizer {

    TreePoint[] srcPoints;


    public TreeBoxDiscretizer(TreePoint[] points) {
        srcPoints = points;
    }


    public VolumeGrid buildGrid(double d, HashMap<String, Double> resHM,
                                double[] surfaceLayers, geometry_t geom, double d2d) {


        TreePoint base = srcPoints[0];
        // TODO - could be better to check if we need to start from a different point,
        // but probably best to require model specification to start with the
        // main segment
        TreeUtil.parentizeFrom(base, srcPoints);
        TreeUtil.orientAC(base, srcPoints);

        SegmentSlicer ss = new SegmentSlicer(srcPoints);


        TreePoint[] slicedPoints = ss.getFixedWidthSlices(d, resHM);

        TreeWriter tw = new TreeWriter(slicedPoints);
        tw.writeSWC(new File("discretized-tree.swc"));

        VolumeGrid vgrid = null;

        switch(geom) {
        case GEOM_2D:
            LineBoxer lb = new LineBoxer(slicedPoints, surfaceLayers, d2d);
            vgrid = lb.buildGrid(d, resHM);
            break;
        case GEOM_3D:
            DiscBoxer db = new DiscBoxer(slicedPoints, surfaceLayers);
            vgrid = db.buildGrid(d, resHM);
            break;
        default:
            throw new RuntimeException("unknown geometry");
        }
        return vgrid;
    }
}
