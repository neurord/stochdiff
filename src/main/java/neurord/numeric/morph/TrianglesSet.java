package neurord.numeric.morph;

import java.util.ArrayList;

import neurord.geom.*;

public class TrianglesSet {

    ArrayList<TriangleStrip> strips;


    public TrianglesSet() {
        strips = new ArrayList<TriangleStrip>();
    }


    public void add(TriangleStrip tss) {
        strips.add(tss);
    }


    public int[] getStripLengths() {
        int[] ret = new int[strips.size()];
        for (int i = 0; i < strips.size(); i++) {
            ret[i] = strips.get(i).getLength();
        }
        return ret;
    }


    public float[][] getPositions() {
        ArrayList<float[]> af = new ArrayList<float[]>();
        for (TriangleStrip ts : strips) {
            ts.addPositions(af);
        }
        return af.toArray(new float[af.size()][]);
    }


    public void rotate(Rotation rot) {
        for (TriangleStrip ts : strips) {
            ts.rotate(rot);
        }
    }


    public void translate(Translation trans) {
        for (TriangleStrip ts : strips) {
            ts.translate(trans);
        }
    }


    public float[][] getNormals() {
        ArrayList<float[]> af = new ArrayList<float[]>();
        for (TriangleStrip ts : strips) {
            ts.addNormals(af);
        }
        return af.toArray(new float[af.size()][]);
    }




}
