package org.catacomb.graph.arbor;


import org.catacomb.be.DeReferencable;
import org.catacomb.be.Position;
import org.catacomb.be.ReReferencable;
import org.catacomb.graph.drawing.VectorIcon;
import org.catacomb.interlish.structure.*;

import java.util.ArrayList;



public class SegmentGraph implements ReReferencable, DeReferencable, PointAttachable,
    MeshInstructor {

    public ArrayList<SegmentGraphPoint> points;
    private ArrayList<AttachmentPoint> p_bufAttachmentPoints;

    private double[] bufLimits;


    public SegmentGraph() {
        points = new ArrayList<SegmentGraphPoint>();
    }


    public void addInitialGraph() {
        SegmentGraphPoint sgp1 = addNewPoint(0., 0., null);
        addNewPoint(10., 0., sgp1);
    }


    public void ensureNonEmpty() {
        if (points.size() == 0) {
            addInitialGraph();
        }
    }



    // for serialization and deserialization;

    public void deReference() {
        // called before serializing to convert private references to storable
        // strings;
        indexPoints();
        for (SegmentGraphPoint sgp : getPoints()) {
            sgp.deReference();
        }
    }



    public void reReference() {
        // called after restoring from file to convert stored values to private
        // references;
        indexPoints();
        for (SegmentGraphPoint sgp : getPoints()) {
            sgp.reReference(points);
        }
    }



    public ArrayList<AttachmentPoint> getAttachmentPoints() {
        // the ones returned could be all or just the labelled ones;
        if (p_bufAttachmentPoints == null || p_bufAttachmentPoints.size() != size()) {

            p_bufAttachmentPoints = new ArrayList<AttachmentPoint>();
            for (SegmentGraphPoint sgp : getPoints()) {
                p_bufAttachmentPoints.add(sgp.makeAttachmentPoint());
            }
        }
        return p_bufAttachmentPoints;
    }



    public Mesh makeMesh(MeshBuilder mbuilder) {
        mbuilder.startMesh();

        indexPoints();
        for (SegmentGraphPoint sgp : getPoints()) {
            Position pos = sgp.getPosition();
            Object newPoint = mbuilder.newPoint(pos.getX(), pos.getY(),
                                                sgp.getZ(), sgp.getR(),
                                                sgp.getIndex(), sgp);

            int i = sgp.getIndex();
            SegmentGraphPoint[] sgn = sgp.getNeighbors();
            for (int k = 0; k < sgn.length; k++) {
                if (sgn[k].getIndex() < i) {
                    mbuilder.connectToPeer(newPoint, sgn[k]);
                }
            }
        }

        Mesh mesh = mbuilder.getMesh();
        return mesh;

    }



    public int size() {
        return getPointCount();
    }


    public int getPointCount() {
        return points.size();
    }


    public ArrayList<SegmentGraphPoint> getPoints() {
        return points;
    }


    public void indexPoints() {
        int i = 0;
        for (SegmentGraphPoint sgp : points) {
            sgp.setIndex(i);
            i += 1;
        }
    }



    public SegmentGraphPoint addNewPoint(double x, double y, SegmentGraphPoint psgp) {
        SegmentGraphPoint sgp = new SegmentGraphPoint(x, y);
        if (psgp != null) {
            psgp.connectTo(sgp);
        }
        addPoint(sgp);
        return sgp;
    }



    public void addRealize(SegmentGraphPoint sgp) {
        addPoint(sgp);
        sgp.realize();
    }



    public void clearHighlight() {
        for (SegmentGraphPoint sgp : points) {
            sgp.unHighlight();
        }
    }


    public void clearMark() {
        for (SegmentGraphPoint sgp : points) {
            sgp.unMark();
        }
    }


    public void highlightPath(SegmentGraphPoint sgpa, SegmentGraphPoint sgpb) {
        clearHighlight();
        clearMark();
        sgpb.highlight();
        sgpa.recHighlightPath();
    }



    public void highlightTree(SegmentGraphPoint sgpa, SegmentGraphPoint sgpb) {
        clearHighlight();
        sgpa.highlight();
        sgpb.recHighlight();
    }


    public void highlightTrace(SegmentGraphPoint sgpa) {
        clearHighlight();
        sgpa.recHighlight();
    }



    public void attachLabelToSelected(String slabel) {
        for (SegmentGraphPoint sgp : points) {
            if (sgp.isHighlighted()) {
                sgp.setLabel(slabel);
            }
        }
    }



    public void showLabel(String s) {
        for (SegmentGraphPoint sgp : points) {
            sgp.highlightLabel(s);
        }
    }



    public void addPoint(SegmentGraphPoint sgp) {
        sgp.setIndex(points.size());
        points.add(sgp);
        p_bufAttachmentPoints = null;

    }


    public void removePoint(SegmentGraphPoint sgp) {
        points.remove(sgp);
        p_bufAttachmentPoints = null;
    }


    public double[] getLimits() {
        if (bufLimits == null) {
            SegmentGraphPoint p0 = points.get(0);
            double xmin = p0.getX();
            double ymin = p0.getY();
            double xmax = xmin;
            double ymax = ymin;
            for (SegmentGraphPoint p : points) {
                double x = p.getX();
                double y = p.getY();
                if (x < xmin) {
                    xmin = x;
                }
                if (x > xmax) {
                    xmax = x;
                }
                if (ymin < y) {
                    ymin = y;
                }
                if (ymax > y) {
                    ymax = y;
                }
            }

            double[] da = { xmin, ymin, xmax, ymax };
            bufLimits = da;
        }
        return bufLimits;
    }



    public VectorIcon getVectorIcon() {
        double[] xyxy = getLimits();
        double xmin = xyxy[0];
        double ymin = xyxy[1];
        double xmax = xyxy[2];
        double ymax = xyxy[3];
        double dx = xmax - xmin + 1.e-4;
        double dy = ymax - ymin + 1.e-4;
        double hdx = (dx / 2);
        double hdy = (dy / 2);
        double xcen = (xmin + xmax) / 2.;
        double ycen = (ymin + ymax) / 2.;


        VectorIcon ret = new VectorIcon();
        for (SegmentGraphPoint p : points) {
            double x0 = (p.getX() - xcen) / hdx;
            double y0 = (p.getY() - ycen) / hdy;
            for (SegmentGraphPoint q : p.getNeighbors()) {
                double x1 = (q.getX() - xcen) / hdx;
                double y1 = (q.getY() - ycen) / hdy;
                ret.addStraightLine(x0, y0, x1, y1);
            }

        }

        return ret;
    }

}
