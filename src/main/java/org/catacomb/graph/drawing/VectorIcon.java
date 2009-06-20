package org.catacomb.graph.drawing;


import java.awt.Color;

import org.catacomb.interlish.content.BasicTouchTime;


public class VectorIcon extends  FixedDrawing {

    public String ref;

    BasicTouchTime touchTime;


    public VectorIcon() {
        super();
        touchTime = new BasicTouchTime();
    }


    public VectorIcon copy() {
        VectorIcon ret = new VectorIcon();
        for (FixedDrawingComponent fdc : items) {
            ret.add(fdc.copy());
        }
        return ret;
    }


    public BasicTouchTime getTouchTime() {
        return touchTime;
    }

    public void baseInit() {
        add(new Square(0., 0., 0.6, 0.6));
    }


    public boolean hasRef() {
        return (ref != null);
    }

    public String getRef() {
        return ref;
    }


    public static VectorIcon errorIcon() {
        VectorIcon vir = new VectorIcon();
        vir.add(Square.defaultSquare());
        return vir;
    }


    public void addSubIcon(VectorIcon icon, double x, double y, double scl) {
        add(new IconWrapper(icon, x, y, scl));
    }


    public static VectorIcon makeErrorIcon() {
        VectorIcon vir = new VectorIcon();
        Disc d = new Disc();
        d.setSize(0.8, 0.8);
        d.setFillColor(Color.red);
        vir.add(d);

        Rectangle r = new Rectangle(0.0, 0.3, 0.1, 0.5);
        r.setFillColor(Color.white);
        vir.add(r);

        Rectangle rd = new Rectangle(0.0, -0.5, 0.1, 0.1);
        rd.setFillColor(Color.white);
        vir.add(rd);
        return vir;
    }


    public static VectorIcon makeRefIcon() {
        VectorIcon vir = new VectorIcon();

        Rectangle r = new Rectangle(0.0, 0.0, 0.8, 0.8);
        r.setClosed();
        r.setLineColor(Color.black);
        r.setLineWidth(1.);
        vir.add(r);
        return vir;

    }



    public static VectorIcon makePendingIcon() {
        VectorIcon vir = new VectorIcon();
        Disc d = new Disc();
        d.setSize(0.8, 0.8);
        d.setFillColor(Color.gray);
        vir.add(d);

        return vir;
    }




}
