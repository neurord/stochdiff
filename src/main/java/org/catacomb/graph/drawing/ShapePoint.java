package org.catacomb.graph.drawing;

import org.catacomb.be.Position;
import org.catacomb.graph.gui.AbsLocated;
import org.catacomb.graph.gui.PickablePoint;



public class ShapePoint implements AbsLocated {

    Shape parentShape;

    Position pAbs;
    Position pCache;

    PickablePoint pickablePoint;

    String type;
    int index;



    public ShapePoint(Shape parent, Position p, int icol) {
        parentShape = parent;
        pAbs = new Position(p);
        pCache = new Position(p);
        pickablePoint = new PickablePoint(p, this, icol);
    }

    public void setColor(int icol) {
        pickablePoint.setColor(icol);
    }


    public void setType(String s) {
        type = s;
    }

    public String getType() {
        return type;
    }

    public boolean isType(String s) {
        return (type != null && type.equals(s));
    }

    public void setIndex(int i) {
        index = i;
    }

    public int getIndex() {
        return index;
    }


    public Shape getParent() {
        return parentShape;
    }

    public void shiftFromCache(Position p) {
        pAbs.set(pCache);
        pAbs.add(p);
    }

    public void setPosition(Position p) {
        pAbs.set(p);
        parentShape.flagPointMoved();
    }



    public Position getAbsolutePosition() {
        return pAbs;
    }

    public Position getPosition() {
        return pAbs;
    }

    public PickablePoint getPickablePoint() {
        return pickablePoint;
    }


    /*
    public void saveOffset(Position pcen) {
       pRel.set(pAbs);
       pRel.subtract(pcen);
    }


    public void applyOffset(Position pcen) {
       pAbs.set(pcen);
       pAbs.add(pRel);
    }

    public Position getPosition() {
       return pAbs;
    }
    */



    public void cachePosition() {
        pCache.set(getPosition());
    }

    public Position getCachedPosition() {
        return pCache;
    }


}
