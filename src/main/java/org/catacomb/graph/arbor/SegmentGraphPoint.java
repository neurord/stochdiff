package org.catacomb.graph.arbor;



import org.catacomb.be.Position;
import org.catacomb.graph.gui.PickablePoint;
import org.catacomb.interlish.content.ConnectionFlavor;
import org.catacomb.interlish.structure.AttachmentPoint;
import org.catacomb.report.E;

import java.awt.Color;
import java.util.ArrayList;



public class SegmentGraphPoint extends PickablePoint {

    // putting all the dm in here for now! REFAC


    int p_status;
    final static int PROTO = 1;
    final static int REAL = 2;


    public double x;
    public double y;
    public double radius;
    public String label;



    ArrayList<SegmentGraphPoint> p_neighbors;
    SegmentGraphPoint[] p_nbrCache;

    public int[] nbrIndexes; // stored when serialized;



    // for real points, need to know about the proto points in thier vicinity;
    SegmentGraphPoint[] p_protoNbrs;
    SegmentGraphPoint p_extensionProto;



    // just for proto points - their neighbors - pN2 may be null;
    SegmentGraphPoint p_pN1;
    SegmentGraphPoint p_pN2;



    int p_index; // for efficiency of graph editing - jump right to point;



    boolean p_highlight;
    boolean p_mark;



    public SegmentGraphPoint() {
        this(0., 0.);
    }


    public SegmentGraphPoint(double x, double y) {
        this(x, y, REAL);
    }


    public SegmentGraphPoint(double x, double y, int stat) {
        super(x, y);

        p_status = stat;


        if (p_status == REAL) {
            setColor(Color.green);
        } else {
            setColor(Color.magenta);
        }


        radius = 1.;
        p_neighbors = new ArrayList<SegmentGraphPoint>();

        if (isReal()) {
            syncExtensionProto();
        }
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void deReference() {
        SegmentGraphPoint[] pa = getNeighbors();
        nbrIndexes = new int[pa.length];
        for (int i = 0; i < pa.length; i++) {
            nbrIndexes[i] = pa[i].getIndex();
        }
        Position pos = getPosition();
        x = pos.getX();
        y = pos.getY();
    }



    public void reReference(ArrayList<SegmentGraphPoint> apa) {
        // System.out.println("rereferenicing a point nbi=" + nbrIndexes);
        setPosition(x, y);

        p_neighbors = new ArrayList<SegmentGraphPoint>();
        if (nbrIndexes != null) {
            for (int i = 0; i < nbrIndexes.length; i++) {
                p_neighbors.add(apa.get(nbrIndexes[i]));
            }
        }
        syncExtensionProto();
    }



    public AttachmentPoint makeAttachmentPoint() {
        SGAttachmentPoint sgap = new SGAttachmentPoint();

        sgap.setPosition(getPosition());
        sgap.setFlavor(new ConnectionFlavor("CellProbe"));
        sgap.setID("" + p_index);

        return sgap;
    }



    public SegmentGraphPoint[] getNeighbors() {
        if (p_nbrCache == null) {
            p_nbrCache = (p_neighbors.toArray(new SegmentGraphPoint[0]));
        }
        return p_nbrCache;
    }



    public void setLabel(String s) {
        label = s;
    }


    public void highlightLabel(String s) {
        if (s.equals(label)) {
            p_highlight = true;
        } else {
            p_highlight = false;
        }
    }


    public boolean isReal() {
        return (p_status == REAL);
    }


    public void unHighlight() {
        p_highlight = false;
    }


    public void highlight() {
        p_highlight = true;
    }


    public boolean isHighlighted() {
        return p_highlight;
    }


    public void unMark() {
        p_mark = false;
    }


    public void mark() {
        p_mark = true;
    }


    public boolean isMarked() {
        return p_mark;
    }



    public void setIndex(int ind) {
        p_index = ind;
    }


    public int getIndex() {
        return p_index;
    }



    public void setRadius(double r) {
        radius = r;
    }


    public double getRadius() {
        return radius;
    }



    public double getR() {
        return getRadius();
    }


    public double getZ() {
        return 0.0; // MISSING
    }


    public SegmentGraphPoint[] getProtoNeighbors() {
        if (p_protoNbrs == null) {
            SegmentGraphPoint[] pa = getNeighbors();
            p_protoNbrs = new SegmentGraphPoint[pa.length];
            for (int i = 0; i < pa.length; i++) {
                if (getIndex() < pa[i].getIndex()) {
                    SegmentGraphPoint psgp = new SegmentGraphPoint(0., 0., PROTO);
                    psgp.setProtoNeighbors(this, pa[i]);
                    p_protoNbrs[i] = psgp;
                }
            }
        }

        return p_protoNbrs;
    }



    public void recHighlight() {
        SegmentGraphPoint[] nbrs = getNeighbors();

        highlight();
        for (int i = 0; i < nbrs.length; i++) {
            if (nbrs[i].isHighlighted()) {
                // nothing to do;
            } else {
                nbrs[i].recHighlight();
            }
        }
    }



    public boolean recHighlightPath() {
        SegmentGraphPoint[] nbrs = getNeighbors();

        mark();
        boolean bret = false;

        if (isHighlighted()) {
            bret = true;

        } else {
            for (int i = 0; i < nbrs.length; i++) {
                if (nbrs[i].isMarked()) {
                    // don't revisit;

                } else {
                    if (nbrs[i].recHighlightPath()) {
                        bret = true;
                        break;
                    }
                }
            }
        }
        if (bret) {
            highlight();
        }
        return bret;
    }



    public void move(Position pos) {
        SegmentGraphPoint[] pa = getNeighbors();
        setPosition(pos);
        if (isReal()) {
            p_extensionProto.updateProtoPosition();

            updateProtos();
            for (int i = 0; i < pa.length; i++) {
                pa[i].updateProtos();
            }
        }

    }


    public void updateProtos() {
        SegmentGraphPoint[] ppa = getProtoNeighbors();
        for (int i = 0; i < ppa.length; i++) {
            if (ppa[i] != null) {
                ppa[i].updateProtoPosition();
            }
        }
    }


    public void realize() {
        if (isReal()) {
            E.error(" - tried to realized real pt");
            return;
        }

        p_status = REAL;
        setColor(Color.green);

        if (p_pN2 == null) {
            setRadius(p_pN1.getRadius());

            connectTo(p_pN1);
            p_pN1.syncExtensionProto();


        } else {
            p_pN1.removeNeighbor(p_pN2);
            p_pN2.removeNeighbor(p_pN1);

            connectTo(p_pN1);
            connectTo(p_pN2);
            setRadius((p_pN1.getRadius() + p_pN2.getRadius()) / 2.);
        }
        p_pN1 = null;
        p_pN2 = null;

        syncExtensionProto();
    }



    public void connectTo(SegmentGraphPoint sgp) {
        addNeighbor(sgp);
        sgp.addNeighbor(this);
    }



    public SegmentGraphPoint getExtensionProto() {
        return p_extensionProto;
    }


    public void removeExtensionProto() {
        p_extensionProto = null;
    }


    public void syncExtensionProto() {
        if (p_extensionProto == null || p_extensionProto.isReal()) {
            Position ppos = getPosition();
            p_extensionProto = new SegmentGraphPoint(ppos.getX(), ppos.getY() - 1.5 * radius, PROTO);
            p_extensionProto.setProtoNeighbors(this, null);
        }
        p_extensionProto.updateProtoPosition();
    }


    public void setProtoNeighbors(SegmentGraphPoint sgp1, SegmentGraphPoint sgp2) {
        p_pN1 = sgp1;
        p_pN2 = sgp2;
        updateProtoPosition();
    }



    public void updateProtoPosition() {
        if (isReal()) {
            E.error(" - called update proto on real point");
        } else {
            Position p1 = p_pN1.getPosition();
            double x1 = p1.getX();
            double y1 = p1.getY();
            double r1 = p_pN1.getRadius();

            if (p_pN2 == null) {
                setPosition(x1, y1 - 1.5 * r1);
            } else {
                Position p2 = p_pN2.getPosition();
                double x2 = p2.getX();
                double y2 = p2.getY();
                setPosition((x1 + x2) / 2., (y1 + y2) / 2.);
            }
        }
    }



    private void addNeighbor(SegmentGraphPoint sgp) {
        p_neighbors.add(sgp);
        p_nbrCache = null;
        p_protoNbrs = null;
    }


    private void removeNeighbor(SegmentGraphPoint sgp) {
        p_neighbors.remove(sgp);
        p_nbrCache = null;
        p_protoNbrs = null;
    }



}
