package org.catacomb.numeric.geom;

public class Carrot {



    public final static double area(double x1, double y1, double z1, double r1,
                                    double x2, double y2, double z2, double r2) {


        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double len2 = dx*dx + dy*dy + dz*dz;

        double dr = r2 - r1;
        double hyp2  = len2 + dr*dr;

        double hyp = Math.sqrt(hyp2);

        /* area is integral from 0 to 1   of  2 * pi * (r1 + f (r2-r1)) len/cos(theta) df;
         = len/cos(theta) * pi * (r1 + r2)

         were tan(theta) = (r2-r1) / len;

         then 1/cos = sqrt(1 + tan2) = sqrt(1 + (r2-r1) (*r2-r1) / len2);
         so len/cos = (sqrt(len2 + (r2-r1)*(r2-r1));
        */


        double ret = Math.PI * hyp * (r1 + r2);

        return ret;
    }



    public final static double conductance(double x1, double y1, double z1, double r1,
                                           double x2, double y2, double z2, double r2) {

        /* resistance is integral from 0 to length  of  dx / area
        = int 0 to 1   length * df / (pi (r1 + f(r2-r1))^2
         = length / (pi * r1 * r2)
            */


        //      double resistance = len / (Math.PI * r1 * r2);
        //      double ret = 1. / resistance;


        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double len2 = dx*dx + dy*dy + dz*dz;
        double length = Math.sqrt(len2);

        double ret = Math.PI * r1 * r2 / length;

        return ret;
    }

}
