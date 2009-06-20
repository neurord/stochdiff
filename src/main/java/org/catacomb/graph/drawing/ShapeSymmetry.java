
package org.catacomb.graph.drawing;



import org.catacomb.be.Position;
import org.catacomb.report.E;


public class ShapeSymmetry {

    // if npt=4 and not extensible, following can apply:
    public final static int NONE = 0;
    public final static int RECTANGLE = 1;
    public final static int DIAMOND = 2;
    public final static int SQUARE = 3;

    public final static String[] symmetryNames= {"none", "rectangle",
                                 "diamond", "square"
                                                };


    public static String[] getSymmetryNames() {
        return symmetryNames;
    }


    public static void applySymmetry(Shape shape, ShapePoint sp, Position pos) {
        int isym = shape.getSymmetry();

        if (isym == NONE) {
            sp.setPosition(pos);

        } else {

            ShapePoint[] spa = shape.getPoints();
            int isp = -1;
            int np = 4; // ADHOC
            for (int i = 0; i < np; i++) {
                if (sp == spa[i]) {
                    isp = i;
                }
            }

            //adjacent clockwise, adjacent anticlockwise and opposite
            ShapePoint adjc = spa[(isp+1) % np];
            ShapePoint opp = spa[(isp+2) % np];
            ShapePoint adja = spa[(isp+3) % np];

            double ox = opp.getPosition().getX();
            double oy = opp.getPosition().getY();
            double dx = pos.getX() - ox;
            double dy = pos.getY() - oy;

            double zx = dx;
            double zy = dy;

            if (isym == SQUARE) {
                double z = Math.sqrt(0.5 * (dx*dx + dy * dy));

                zx = (dx > 0 ? z : -z);
                zy = (dy > 0 ? z : -z);

            } else if (isym == RECTANGLE) {
                // leave as is;
            } else {
                E.missing("sym=" + isym);
            }

            adjc.setPosition(new Position(ox +zx, oy));
            adja.setPosition(new Position(ox, oy + zy));
            sp.setPosition(new Position(ox + zx, oy + zy));
        }
    }


    public static String getStringSymmetry(int isym) {
        return symmetryNames[isym];
    }


}
