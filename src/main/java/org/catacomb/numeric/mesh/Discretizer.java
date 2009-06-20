
package org.catacomb.numeric.mesh;

import java.util.ArrayList;


public abstract class Discretizer {



    /**
     *    divided into segments with equal integral of
     *    the square root of the radius, given by disqrtr.
     *    This balances the charging rate of points in the final
     *    discretization and its definition is independent of
     *    the electrical properties of the membrane.
     *    It provides a more consistent discretization than the
     *    electrotonic length, and is valid in the absence of
     *    persistent currents.
     *
     *    Tapering segments are treated as such and not approximated by a series
     *    of cylinders. The discretization does, however, respect the
     *    actual points of the structure to which clamps or recorders
     *    may be attached.
     */


    public static MeshPoint[] discretize(MeshPoint[] pt,
                                         double disqrtr, int maxnpt) {

        int np = pt.length;
        double[][][] subdiv = new double[np][6][];
        for (int i = 0; i < np; i++) {
            pt[i].setWork(i);
        }

        int nnp = 0;
        for (int i = 0; i < np; i++) {
            MeshPoint cpa = pt[i];

            int nnbr = cpa.getNeighborCount();
            MeshPoint[] anbrs = cpa.getNeighbors();

            for (int j = 0; j < nnbr; j++) {
                MeshPoint cpb = anbrs[j];

                if (cpa.getWork() < cpb.getWork()) {
                    subdiv[i][j] = getSubdivision(cpa, cpb, disqrtr);
                    nnp += subdiv[i][j].length;
                }
            }
        }

        if (np + nnp > maxnpt) {
            Sp("WARNING - not discretizing: needs too many points ("+np+nnp+")");
            return null;
        }

        MeshPoint[] pr = new MeshPoint[np+nnp];
        for (int i = 0; i < np; i++) {
            pr[i] = pt[i];
        }
        int nxp = np;

        for (int i = 0; i < np; i++) {
            MeshPoint cpa = pt[i];

            int nnbr = cpa.getNeighborCount();
            MeshPoint[] anbrs = cpa.getNeighbors();

            for (int j = 0; j < nnbr; j++) {
                double[] div = subdiv[i][j];
                if (div != null && div.length > 0) {

                    MeshPoint cpb = anbrs[j];
                    MeshPoint clast = null;

                    for (int id = 0; id < div.length; id++) {

                        MeshPoint cp = cpa.newPoint();

                        locateBetween(cpa, cpb, div[id], cp);
                        pr[nxp++] = cp;
                        if (id == 0) {
                            cpa.replaceNeighbor(cpb, cp);
                            cp.addNeighbor(cpa);
                        }
                        if (id == div.length-1) {
                            cpb.replaceNeighbor(cpa, cp);
                            cp.addNeighbor(cpb);
                        }
                        if (clast != null) {
                            cp.addNeighbor(clast);
                            clast.addNeighbor(cp);
                        }
                        clast = cp;
                    }
                }
            }
        }
        return pr;
    }



    public static void locateBetween(MeshPoint cpa, MeshPoint cpb, double f,
                                     MeshPoint cpn) {
        double wf = 1. - f;
        cpn.setX(f * cpb.getX() + wf * cpa.getX());
        cpn.setY(f * cpb.getY() + wf * cpa.getY());
        cpn.setZ(f * cpb.getZ() + wf * cpa.getZ());
        cpn.setR(f * cpb.getR() + wf * cpa.getR());

    }



    public static double distanceBetween(MeshPoint cp, MeshPoint cq) {
        double dx = cp.getX() - cq.getX();
        double dy = cp.getY() - cq.getY();
        double dz = cp.getZ() - cq.getZ();
        double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
        return d;
    }



    public static void movePerp(MeshPoint ca, MeshPoint cb, double dperp,
                                MeshPoint cm) {
        double dx = cb.getX() - ca.getX();
        double dy = cb.getY() - ca.getY();
        double f = Math.sqrt(dx*dx + dy*dy);
        dx /= f;
        dy /= f;
        double x = cm.getX();
        double y = cm.getY();
        x += dperp * dy;
        y -= dperp * dx;

        cm.setX(x);
        cm.setY(y);
    }



    public static MeshPoint[] merge(MeshPoint[] pt,
                                    double maxdr, double maxlen,
                                    double tol) {


        for (int i = 0; i < pt.length; i++) {
            pt[i].setWork(2);
        }


        MeshPoint p0 = pt[0];
        recMerge(p0, maxdr, maxlen, tol);

        // remove dead points.
        int nl = 0;
        for (int i = 0; i < pt.length; i++) {
            if (pt[i].getWork() > 0) {
                nl++;
            }
        }
        MeshPoint[] pr = p0.newPointArray(nl);
        nl = 0;
        for (int i = 0; i < pt.length; i++) {
            if (pt[i].getWork() > 0) {
                pr[nl++] = pt[i];
            }
        }
        return pr;
    }



    public static void recMerge(MeshPoint cp, double maxdr, double maxlen,
                                double tol) {
        // may have already been done
        cp.setWork(1);

        int nnbr = cp.getNeighborCount();
        MeshPoint[] anbrs = cp.getNeighbors();

        for (int j = 0; j < nnbr; j++) {
            MeshPoint cq = anbrs[j];

            double rp = cp.getR();
            double rq = cq.getR();


            if (cq.getWork() == 2 &&
                    cq.getNeighborCount() == 2 &&
                    Math.abs((rq - rp) / (rq + rp)) < 0.5 * maxdr &&
                    distanceBetween(cp, cq) < maxlen) {

                // if nnbr is not two, either it is a terminal, or a branch point:
                // in either case there is nothing to do;

                MeshPoint cprev = cp;
                ArrayList<MeshPoint> vpt = new ArrayList<MeshPoint>();
                double ltot = 0.;
                double ldtot = 0.;


                while (cq.getNeighborCount() == 2 &&
                        distanceBetween(cprev, cq) < maxlen &&
                        Math.abs((rq-rp)/(rq+rp)) < 0.5 * maxdr) {
                    vpt.add(cq);
                    double dl = distanceBetween(cprev, cq);
                    ltot += dl;
                    ldtot += dl * (cprev.getR() + cq.getR());

                    MeshPoint cnxt;
                    MeshPoint[] acqn  = cq.getNeighbors();
                    if (acqn[0] == cprev) {
                        cnxt = acqn[1];
                    } else {
                        cnxt = acqn[0];
                    }

                    cprev = cq;
                    cq = cnxt;
                    rq = cq.getR();
                }

                double dl = distanceBetween(cprev, cq);
                ltot += dl;
                ldtot += dl * (cprev.getR() + cq.getR());


                double lab = distanceBetween(cp, cq);
                double ldab = lab * (cp.getR() + cq.getR());

                int nadd = (int)(ltot / maxlen);

                if (nadd > vpt.size()) {
                    nadd = vpt.size(); //cant happen at present;
                }
                // recycle nadd points;

                boolean cor = (Math.abs((lab-ltot)/(lab+ltot)) > 0.5 * tol ||
                               Math.abs((ldab-ldtot)/(ldab+ldtot)) > 0.5 * tol);
                if (cor && nadd == 0) {
                    nadd = 1;
                }

                if (nadd == 0) {
                    cp.replaceNeighbor(vpt.get(0), cq);
                    cq.replaceNeighbor(vpt.get(vpt.size()-1), cp);

                } else {

                    for (int i = 0; i < nadd; i++) {
                        MeshPoint cm = vpt.get(i);
                        cm.setWork(1);
                        locateBetween(cp, cq, (1.+ i)/(1.+ nadd), cm);
                        if (i == nadd-1 && nadd < vpt.size()) {
                            cm.replaceNeighbor(vpt.get(nadd), cq);
                            cq.replaceNeighbor(vpt.get(vpt.size()-1), cm);
                        }
                    }
                }


                // just kill the rest;
                for (int jd = nadd; jd < vpt.size(); jd++) {
                    MeshPoint cd = vpt.get(jd);
                    cd.disconnect();
                    cd.setWork(0);
                }


                if (cor) {
                    double dpar = lab / (nadd+1);
                    double fl = ltot / lab;
                    double dperp = Math.sqrt((fl*fl - 1) * dpar*dpar);
                    double fr = ldtot / (fl * ldab);
                    for (int i = 0; i < nadd; i++) {
                        MeshPoint cm = vpt.get(i);

                        double cmr = cm.getR();
                        cm.setR(cmr * fr);

                        if (i % 2 == 0) {
                            movePerp(cp, cq, ((i/2) % 2 == 0 ? dperp : - dperp), cm);
                        }
                    }

                }




            }
            if (cq.getWork() == 2) {
                recMerge(cq, maxdr, maxlen, tol);
            }
        }
    }





    public static double[] getSubdivision(MeshPoint cpa, MeshPoint cpb,
                                          double disqrtr) {
        double dab = distanceBetween(cpa, cpb);
        double ra = cpa.getR();
        double rb = cpb.getR();



        double fdist = 0.0;
        // fdist is to be the integral in question between pta and ptb;
        if (rb != ra) {
            fdist = ((2./3.) * dab  / (rb - ra) *
                     (Math.pow(rb, 3./2.) - Math.pow(ra, 3./2.)));
//	lbya = dab * (1./rb - 1./ra) / (Math.PI * (ra - rb));

        } else {
            fdist = dab * Math.sqrt(ra);
//	lbya = dab / (Math.PI * ra * ra);
        }
//     double aseg = dab * Math.PI * (ra + rb);

        int nadd = (int)(fdist / disqrtr);

        double[] dpos = new double[nadd];

        if (nadd > 0) {

            if (Math.abs((ra-rb)/(ra+rb)) < 0.01) {
                for (int i = 0; i < nadd; i++) dpos[i] = (1.+i) / (nadd+1.);

            } else {
                // chop up the carrot;
                double delf = fdist / (nadd+1);
                double ffa = (rb - ra) / dab;      // dr/dx
                double xa = ra / ffa;
                double xb = rb / ffa;
                // xa and xb are the end positions measured from where
                // the carrot comes to a point.
                double x = xa;

                // the integral of sqrt(r) dx is
                // 2/3 * dx / (rb-ra) * (rb^3/2 - ra^3/2)
                // so need dx such that this is delf (= total_int / nseg)

                for (int i = 0; i < nadd+1; i++) {
                    double ttt = (delf * ffa * 3./2. +
                                  Math.pow(ffa * x, 3./2.));
                    double dx = Math.pow(ttt, (2./3.)) / ffa - x;
                    x += dx;
                    if (i < nadd) {
                        dpos[i] = (x - xa) / dab;
                    }
                }
                if (Math.abs(xb - x) > 1.e-5) {
                    Sp("ERROR : ECNet segment division " + xa + " " + xb +
                       " " + x + " " + nadd + " " +dab + " " +ra+ " " + rb);
                }
            }
        }
        return dpos;
    }


    public static void Sp(String s) {
        System.out.println(s);
    }


}



