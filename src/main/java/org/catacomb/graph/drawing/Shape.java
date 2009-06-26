package org.catacomb.graph.drawing;


import java.awt.Color;

import org.catacomb.be.DeReferencable;
import org.catacomb.be.Position;
import org.catacomb.be.ReReferencable;
import org.catacomb.datalish.SColor;
import org.catacomb.graph.gui.Geom;
import org.catacomb.graph.gui.PickableRegion;
import org.catacomb.interlish.content.*;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;
import org.catacomb.util.ArrayUtil;


public class Shape extends Polypoint
    implements ReReferencable, DeReferencable, TablePeer {


    public double curviness; // 0 rectangle, 1 circle, in between for rounded
    // corners;


    public double lineWidth;

    public SColor lineColor;
    public SColor fillColor;

    private Color p_lineColor;
    private Color p_fillColor;





    public String symmetry;
    private int p_symmetry; // values in ShapeSymmetry


    // strategies for resolving point motions;
    public final static int MOVE_POINT = 0;
    public final static int ROTATE_SHAPE = 1;
    public final static int SHIFT_SHAPE = 2;



    private ShapePoint[] p_points;
    private ShapePoint[] p_protos;
    private boolean p_protosUTD;


    private Position p_position;
    private Position p_cachePosition;
    private Position p_pressPosition;


    private PickableRegion p_pickableRegion;

    private boolean updatePickable;


    private IntPosition p_intPosition;
    private boolean intIsDefinitive;


    private Assembly r_parent;

    private boolean pointLeadsArrays;
    private boolean p_rotating;

    private int pointColor = 0x00ff00;
    private int protoPointColor = 0xff00ff;



    private int p_index;

    private Table p_table;


    public Shape() {
        p_position = new Position();
        p_cachePosition = new Position();
        p_lineColor = Color.blue;
        p_fillColor = Color.yellow;
        lineWidth = 1.0;
        curviness = 0.;
        setClosure(FILLED);
    }


    public Shape(Shape s) {
        this();
        curviness = s.curviness;
        p_symmetry = s.p_symmetry;
        setClosure(s.getClosure());
        lineWidth = s.lineWidth;
        p_lineColor = s.p_lineColor;
        p_fillColor = s.p_fillColor;

        xpts = copyArray(s.xpts);
        ypts = copyArray(s.ypts);

        makePoints();

        syncArrays();

        initPickable();

    }



    public void setLineWidth(double d) {
        lineWidth = d;
    }

    public void setIndex(int ind) {
        p_index = ind;
    }

    public int getIndex() {
        return p_index;
    }


    public void select() {
        cachePositions();
    }


    public ShapePoint[] getPoints() {
        return p_points;
    }


    public ShapePoint[] getProtoPoints() {
        if (p_protos == null || !p_protosUTD) {
            makeProtos();
        }
        return p_protos;
    }

    public String getStringClosure() {
        return p_closureNames[getClosure()];
    }

    public void setParent(Assembly a) {
        r_parent = a;
    }


    public Assembly getParent() {
        return r_parent;
    }



    public void setCurviness(double d) {
        curviness = d;
    }

    public double getCurviness() {
        return curviness;
    }



    public void setClosure(String s) {
        setClosure(OPEN);
        int iia = ArrayUtil.getIndexInArray(s, p_closureNames);
        if (iia >= 0) {
            setClosure(iia);
        }
    }



    private double[] copyArray(double[] da) {
        double[] ret = new double[da.length];
        System.arraycopy(da, 0, ret, 0, da.length);
        return ret;
    }


    public Shape makeCopy() {
        syncArrays();
        return new Shape(this);
    }


    public void setSymmetryString(String s) {
        p_symmetry = ShapeSymmetry.NONE;
        int iia = ArrayUtil.getIndexInArray(s, ShapeSymmetry.getSymmetryNames());
        if (iia >= 0) {
            p_symmetry = iia;
        }
    }


    public void reReference() {
        setClosure(closure);

        setSymmetryString(symmetry);

        makePoints();

        syncArrays();

        if (lineColor != null) {
            p_lineColor = lineColor.getColor();
        }
        if (fillColor != null) {
            p_fillColor = fillColor.getColor();
        }
    }



    public void deReference() {
        closure = p_closureNames[getClosure()];
        String[] symnms = ShapeSymmetry.getSymmetryNames();
        symmetry = symnms[p_symmetry];
    }



    public double getSmoothness() {
        return curviness;
    }


    public Color getLineColor() {
        return p_lineColor;
    }


    public Color getFillColor() {
        return p_fillColor;
    }


    public double getLineWidth() {
        return lineWidth;
    }


    public boolean isExtensible() {
        return (p_symmetry == ShapeSymmetry.NONE);
    }


    public int getSymmetry() {
        return p_symmetry;
    }



    private void cachePositions() {
        p_cachePosition.set(p_position);

        for (int i = 0; i < p_points.length; i++) {
            p_points[i].cachePosition();
        }

    }



    public void setPosition(Position p) {
        updatePickable = true;

        Position prel = new Position(p);
        prel.subtract(p_cachePosition);

        for (int i = 0; i < p_points.length; i++) {
            p_points[i].shiftFromCache(prel);
        }

        flagPointMoved();
        // syncArrays();
        intIsDefinitive = false;
    }



    public void initPickable() {
        p_pickableRegion = new PickableRegion(getXPts(), getYPts(), this);
        if (!isClosed()) {
            p_pickableRegion.setPoints(Geom.makeLineBoundary(getXPts(), getYPts()));
        }
        p_pickableRegion.setReferencePoint(p_position);
        updatePickable = false;
    }



    public PickableRegion getBoundaryRegion() {
        if (p_pickableRegion == null) {
            initPickable();

        } else if (updatePickable) {
            if (isClosed()) {
                p_pickableRegion.setPoints(getXPts(), getYPts());
                p_pickableRegion.setReferencePoint(p_position);
            } else {
                p_pickableRegion.setPoints(Geom.makeLineBoundary(getXPts(), getYPts()));
                p_pickableRegion.setReferencePoint(p_position);
            }

            updatePickable = false;
        }
        return p_pickableRegion;
    }


    public void makePoints() {
        int np = xpts.length;
        p_points = new ShapePoint[np];
        for (int i = 0; i < np; i++) {
            p_points[i] = new ShapePoint(this, new Position(xpts[i], ypts[i]), pointColor);
        }
    }



    private void makeProtos() {
        int np = p_points.length;
        int nproto = np;
        if (isOpen()) {
            nproto -= 1;
        }

        p_protos = new ShapePoint[nproto];

        for (int i = 0; i < nproto; i++) {
            Position pa = p_points[i].getPosition();
            Position pb = p_points[(i + 1) % np].getPosition();

            Position midpos = Position.aXPlusBY(0.5, pa, 0.5, pb);
            ShapePoint sp = new ShapePoint(this, midpos, protoPointColor);
            p_protos[i] = sp;
            sp.setIndex(i);
            sp.setType("proto");
        }
        p_protosUTD = true;
    }


    public Position getPosition() {
        return p_position;
    }


    public void setIntPosition(IntPosition intp) {
        if (p_intPosition == null) {
            p_intPosition = new IntPosition();
        }
        p_intPosition.set(intp);
        intIsDefinitive = true;
    }


    public boolean hasIntPosition() {
        return intIsDefinitive;
    }


    public IntPosition getIntPosition() {
        return p_intPosition;
    }



    public double[] getXPts() {
        if (pointLeadsArrays) {
            syncArrays();
        }
        return xpts;
    }


    public double[] getYPts() {
        if (pointLeadsArrays) {
            syncArrays();
        }
        return ypts;
    }


    public void flagPointMoved() {
        pointLeadsArrays = true;
        p_protosUTD = false;
    }


    public void syncArrays() {
        int np = p_points.length;
        if (xpts == null || xpts.length != np) {
            xpts = new double[np];
            ypts = new double[np];
        }

        for (int i = 0; i < np; i++) {
            Position pos = p_points[i].getPosition();
            xpts[i] = pos.getX();
            ypts[i] = pos.getY();
        }

        p_position.set(cog(xpts), cog(ypts));

        p_protosUTD = false;
        updatePickable = true;
        pointLeadsArrays = false;
    }



    public void addPoint(int ipr, double x, double y) {
        addPoint(ipr, new ShapePoint(this, new Position(x, y), pointColor));
    }


    public void addPoint(int ipr, ShapePoint sp) {
        sp.setType("normal");
        sp.setColor(pointColor);

        int np = p_points.length;
        ShapePoint[] ap = new ShapePoint[np + 1];

        for (int i = 0; i <= ipr; i++) {
            ap[i] = p_points[i];
        }

        ap[ipr + 1] = sp;

        for (int i = ipr + 1; i < np; i++) {
            ap[i + 1] = p_points[i];
        }

        p_points = ap;

        p_protosUTD = false;
        syncArrays();
    }


    public boolean deletePoint(ShapePoint sp) {
        boolean ret = false;
        int itg = -1;
        for (int i = 0; i < p_points.length; i++) {
            if (sp == p_points[i]) {
                itg = i;
                break;
            }
        }
        if (itg >= 0) {
            ret = deletePoint(itg);
        } else {
            E.error("cant find point");
        }
        return ret;
    }


    public boolean deletePoint(int itg) {
        boolean bdone = false;
        if (isRectangular()) {
            // cant delete;
        } else if (p_points.length <= 2) {
            // cante delete - kill whole shape;
        } else {
            int np = p_points.length;
            ShapePoint[] ap = new ShapePoint[np -1];
            for (int i = 0; i < itg; i++) {
                ap[i] = p_points[i];
            }
            for (int i = itg; i < np-1; i++) {
                ap[i] = p_points[i+1];
            }
            p_points = ap;
            p_protosUTD = false;
            syncArrays();
            bdone = true;
        }
        return bdone;
    }


    private double cog(double[] ap) {
        int n = ap.length;
        double c = 0.;
        for (int i = 0; i < n; i++) {
            c += ap[i];
        }
        c /= n;
        return c;
    }


    public void regionPressed() {
        p_rotating = false;
    }

    public void pointPressed(ShapePoint sp) {
        p_rotating = false;
        cachePositions();
    }


    public void movePoint(ShapePoint sp, Position pos,  int action) {

        if (action == SHIFT_SHAPE) {
            // shift(xnew - pcache.getX(), ynew - pcache.getY());


        } else if (action == ROTATE_SHAPE) {
            rotate(sp, pos);


        } else if (action == MOVE_POINT) {
            ShapeSymmetry.applySymmetry(this, sp, pos);
        }

        flagPointMoved();
    }


    public void rotate(Position pos) {
        if (p_rotating) {
            rotateFromTo(p_pressPosition, pos);
        } else {
            p_pressPosition = new Position(pos);
            p_rotating = true;
        }
    }

    private void rotate(ShapePoint sp, Position pos) {
        rotateFromTo(sp.getCachedPosition(), pos);
    }

    private void rotateFromTo(Position pc, Position pos) {
        double pxc = p_cachePosition.getX();
        double pyc = p_cachePosition.getY();

        double xa = pc.getX() - pxc;
        double ya = pc.getY() - pyc;
        double xb = pos.getX() - pxc;
        double yb = pos.getY() - pyc;

        double a2 = xa*xa + ya*ya;
        double b2 = xb*xb + yb*yb;

        double mp = Math.sqrt(a2 * b2);
        double cos = (xa*xb + ya*yb) / mp;
        double sin = (xa*yb - xb*ya) / mp;


        for (int i = 0; i < p_points.length; i++) {
            Position pca = p_points[i].getCachedPosition();
            double px = pca.getX() - pxc;
            double py = pca.getY() - pyc;
            double pxn =  pxc +  cos * px - sin * py;
            double pyn = pyc + sin * px + cos * py;
            p_points[i].setPosition(new Position(pxn, pyn));
        }

    }


    public void setLineColor(Color col) {
        p_lineColor = col;
    }


    public void setFillColor(Color col) {
        p_fillColor = col;
    }


    public boolean isRectangular() {

        return (p_symmetry == ShapeSymmetry.RECTANGLE ||
                p_symmetry == ShapeSymmetry.SQUARE);
    }

    public boolean overlaps(Shape shape) {
        // TODO - should work it out!!!
        return true;
    }


    public void attachTable(Table tbl) {
        p_table = tbl;
    }

    public void initFromTable(Table tbl) {
        p_table = tbl;
    }


    public Table getTable() {
        return p_table;
    }

    public void removeChild(TablePeer child) {
        E.error("shapes dont have children");
    }

    public void setSymmetry(int isym) {
        p_symmetry = isym;
    }


    public void rescale(double d) {
        expand(d);
    }


    public void expand(double d) {
        for (int i = 0; i < xpts.length; i++) {
            xpts[i] *= d;
            ypts[i] *= d;
        }
        makePoints();
        syncArrays();
    }

    public void shiftExpand(double ox, double oy, double d) {
        for (int i = 0; i < xpts.length; i++) {
            xpts[i] = ox + d * xpts[i];
            ypts[i] = oy + d * ypts[i];
        }
        makePoints();
        syncArrays();
    }


    public void contract(double d) {
        expand(1. / d);
    }


    public String getStringSymmetry() {
        return ShapeSymmetry.getStringSymmetry(p_symmetry);
    }


    public boolean isQuadrilateral() {
        return (xpts.length == 4);
    }


    public boolean isFullyRounded() {
        return curviness > 0.99;
    }


    public boolean isFullyAngular() {
        return curviness < 0.01;
    }


    public boolean isSymmetric() {
        return (p_symmetry == ShapeSymmetry.RECTANGLE  ||
                p_symmetry == ShapeSymmetry.SQUARE);
    }


    public boolean isRectangleSymmetry() {
        return (p_symmetry == ShapeSymmetry.RECTANGLE);

    }


    public boolean isSquareSymmetry() {
        return (p_symmetry == ShapeSymmetry.SQUARE);

    }


    public void notifyObservers() {
        // TODO Auto-generated method stub

    }

// REFAC - to go in singleton
    private double[][] curveInterps() {
        int nintern = 5;  // ADHOC - 5 - smoothness...

        double[][] cif = new double[2][nintern];
        for (int i = 0; i < nintern; i++) {
            double fi = (1. * i) / (nintern);

            cif[0][i] = Math.sin(fi * Math.PI / 2.);
            // cif[1][i] = Math.cos(fi * Math.PI / 2.);
            cif[1][i] = 1. - Math.sin((1. - fi) * Math.PI / 2.);
        }
//  E.dump("cif0", cif[0]);
//  E.dump("cif1", cif[1]);

        return cif;
    }





    public double[][] getBoundaryPoints() {
        double[][] ret = null;
        ShapePoint[] spts = getPoints();
        int n = spts.length;
        if (curviness <= 0.1) {
            // just return the points themselves
            ret = new double[2][n];
            for (int i = 0; i < n; i++) {
                ShapePoint sp = spts[i];
                Position ap = sp.getAbsolutePosition();
                ret[0][i] = ap.getX();
                ret[1][i] = ap.getY();
            }
        } else {
            // assumes closed shapes...
            double[][] cif = curveInterps();
            int nin = cif[0].length;
            ret = new double[2][n * nin];
            for (int ipt = 0; ipt < n; ipt++) {
                Position pa = spts[ipt].getAbsolutePosition();
                Position pb = spts[(ipt+1)%n].getAbsolutePosition();
                Position pc = spts[(ipt+2)%n].getAbsolutePosition();

                Position mab = Position.midpoint(pa, pb);
                Position mbc = Position.midpoint(pb, pc);
                double v1x = pb.getX() - mab.getX();
                double v1y = pb.getY() - mab.getY();
                double v2x = mbc.getX() - pb.getX();
                double v2y = mbc.getY() - pb.getY();
                for (int j = 0; j < nin; j++) {
                    int iin = ipt * nin + j;
                    ret[0][iin] = mab.getX() + cif[0][j] * v1x + cif[1][j] * v2x;
                    ret[1][iin] = mab.getY() + cif[0][j] * v1y + cif[1][j] * v2y;
                }
            }
        }
        return ret;
    }



    public RShape exportRunish(double f) {
        double[][] xy = getBoundaryPoints();
        for (int i = 0; i < xy[0].length; i++) {
            xy[0][i] *= f;
            xy[1][i] *= f;
        }


        int rsc = RShape.CLOSED;

        if (closure.equals("open")) {
            rsc = RShape.OPEN;
        } else if (closure.equals("closed")) {
            rsc = RShape.CLOSED;
        } else if (closure.equals("filled")) {
            rsc = RShape.FILLED;
        } else {
            E.error("unrecognized " + closure);
        }
        RShape rs = new RShape(xy[0], xy[1], lineWidth, lineColor, fillColor, rsc);
        return rs;
    }

}
