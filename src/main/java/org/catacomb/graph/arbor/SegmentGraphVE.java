
package org.catacomb.graph.arbor;


import org.catacomb.be.Position;
import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.*;
import org.catacomb.report.E;

import java.awt.Color;


public class SegmentGraphVE implements BuildPaintInstructor, PickListener {


    SegmentGraph graph;

    double pressX;
    double pressY;
    double pressRadius;


    boolean selectable;
    boolean modifiable;
    boolean antialias;


    int selectAction;
    public final static int VIEW = 0;
    public final static int EDIT = 1;
    public final static int MOVE = 2;
    public final static int DELETE = 3;
    public final static int PATH = 4;
    public final static int TREE = 5;
    public final static int TRACE = 6;

    final static String[] actionNames = {"view", "edit", "move", "delete",
                                         "selectPath", "selectTree", "trace"
                                        };


    SegmentGraphPoint sgp1;
    SegmentGraphPoint sgp2;


    public SegmentGraphVE() {
        antialias = true;
        selectAction = 0;
    }


    public void setSegmentGraph(SegmentGraph sg) {
        graph = sg;
    }



    public void setAntialias(boolean b) {
        antialias = b;
    }



    public void setSelectAction(String sin) {
        String s = sin;
        if (s == null) {
            s = "view";
        }
        int iact = -1;
        for (int i = 0; i < actionNames.length; i++) {
            if (actionNames[i].equals(s)) {
                iact = i;
            }
        }

        if (iact >= 0) {
            setSelectAction(iact);
        } else {
            E.error("dont know action " + s);
        }
    }


    public void setSelectAction(int act) {
        selectAction = act;
        clearSelect();

    }


    public void clearSelect() {
        sgp1 = null;
        sgp2 = null;
    }



    public boolean antialias() {
        return antialias;
    }





    public void instruct(Painter painter, Builder builder) {
        if (graph == null) {
            return;
        }

        // paint the protos first;


        if (selectAction == EDIT) {
            for (SegmentGraphPoint sgp : graph.getPoints()) {
                paintProto(sgp, painter, builder);
            }
        }


        for (SegmentGraphPoint sgp : graph.getPoints()) {
            paintReal(sgp, painter, builder);
        }

    }




    public void paintProto(SegmentGraphPoint sgp,
                           Painter painter, Builder builder) {
        SegmentGraphPoint sgpe = sgp.getExtensionProto();

        painter.setColor(Color.gray);
        painter.drawLine(sgp.getPosition(), sgpe.getPosition());

        builder.addPoint(sgpe);


        SegmentGraphPoint[] psgps = sgp.getProtoNeighbors();
        SegmentGraphPoint[] rsgps = sgp.getNeighbors();

        int index = sgp.getIndex();
        for (int i = 0; i < psgps.length; i++) {
            if (index < rsgps[i].getIndex()) {
                builder.addPoint(psgps[i]);
            }
        }

    }






    public void paintReal(SegmentGraphPoint sgp,
                          Painter painter, Builder builder) {

        if (selectAction != VIEW) {
            builder.addPoint(sgp);
        }

        SegmentGraphPoint[] nbrs = sgp.getNeighbors();
        int index = sgp.getIndex();


        if (sgp.isHighlighted()) {
            painter.setColor(Color.orange);
            painter.fillCircle(sgp.getPosition(), sgp.getRadius());

        } else {
            painter.setColor(Color.white);
            painter.drawCircle(sgp.getPosition(), sgp.getRadius());
        }



        for (int i = 0; i < nbrs.length; i++) {
            if (nbrs[i].getIndex() > index) {
                if (sgp.isHighlighted() && nbrs[i].isHighlighted()) {
                    painter.setColor(Color.orange);
                } else {
                    painter.setColor(Color.white);
                }
                drawSegment(painter, sgp, nbrs[i]);
            }
        }
    }


    public void drawSegment(Painter painter,
                            SegmentGraphPoint sgpa, SegmentGraphPoint sgpb) {
        Position p1 = sgpa.getPosition();
        double x1 = p1.getX();
        double y1 = p1.getY();
        double r1 = sgpa.getRadius();

        Position p2 = sgpb.getPosition();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double r2 = sgpb.getRadius();


        painter.drawCarrotSides(x1, y1, r1,  x2, y2, r2);
    }



    public void backgroundPressed(int i, int x, int y) {

    }


    public void pickPressed(Pickable pbl, int button, int ix, int iy) {

        if (pbl instanceof SegmentGraphPoint) {
            SegmentGraphPoint sgp = (SegmentGraphPoint)pbl;



            if (button == Mouse.LEFT) {
                if (sgp.isReal()) {

                    tryHighlight(sgp);

                } else {
                    graph.addRealize(sgp);
                }
            } else if (button == Mouse.RIGHT) {
                if (sgp.isReal()) {
                    pressRadius = sgp.getRadius();
                    pressX = sgp.getPosition().getX();
                    pressY = sgp.getPosition().getY();
                }
            }
        }
    }




    public void pickDragged(Pickable pbl, Position pos, int button, int ix, int iy) {

        if (selectAction == EDIT || selectAction == MOVE) {
            if (pbl instanceof SegmentGraphPoint) {
                SegmentGraphPoint sgp = (SegmentGraphPoint)pbl;

                if (button == Mouse.LEFT) {
                    sgp.move(pos);

                } else if (button == Mouse.RIGHT) {
                    double f = 0.1 * (pos.getY() - pressY) / pressRadius;
                    sgp.setRadius(pressRadius * Math.exp(f));

                }
            }
        }

    }



    public void pickReleased(Pickable pbl, int button) {


    }


    private void tryHighlight(SegmentGraphPoint sgp) {
        sgp1 = sgp2;
        sgp2 = sgp;

        if (selectAction == TRACE) {
            graph.highlightTrace(sgp);


        } else if (sgp1 != null) {

            if (selectAction == PATH) {
                graph.highlightPath(sgp1, sgp2);
                clearSelect();

            } else if (selectAction == TREE) {
                graph.highlightTree(sgp1, sgp2);
                clearSelect();
            }

        } else {
            graph.clearHighlight();
            sgp2.highlight();
        }
    }

    public void pickEnteredTrash(Pickable pbl) {
    }

    public void pickLeftTrash(Pickable pbl) {
    }

    public void pickTrashed(Pickable pbl) {
    }


    public void trashPressed() {
        // TODO Auto-generated method stub

    }


    public void pickHovered(Pickable hoverItem) {
        // TODO Auto-generated method stub

    }


    public Box getLimitBox() {
        // TODO Auto-generated method stub
        return null;
    }


    public Box getLimitBox(Painter p) {
        // TODO Auto-generated method stub
        return null;
    }



}
