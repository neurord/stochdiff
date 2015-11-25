//6 26 2007: WK modified the discretize() function to make all volume elements
//           be a-by-a squares where a is the 'max element side' calculated
//           in the extractGrid() function in BaseCalc.java
//written by Robert Cannon
package neurord.disc;

import java.util.ArrayList;

import neurord.numeric.morph.TreePoint;

import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * divided into segments either with fixed lengths or with
 * equal integral of the square root of the radiusr.
 * Th sqrt(r) form balances the charging rate of points in the final
 * discretization and its definition is independent of the electrical properties
 * of the membrane. It provides a more consistent discretization than the
 * electrotonic length, and is valid in the absence of persistent currents.
 *
 * Tapering segments are treated as such and not approximated by a series of
 * cylinders. The discretization respects the actual points of the
 * structure to which stimuli or recorders may be attached.
 */
public class SegmentSlicer {
    static final Logger log = LogManager.getLogger();

    TreePoint[] srcPoints;

    final static int FIXED = 1;
    final static int BALANCED = 2;
    int sdstyle;
    int maxnp;



    TreePoint[] outPoints;

    Resolution resolution;


    public SegmentSlicer(TreePoint[] sp) {
        srcPoints = sp;
        /*
           for (int i = 0; i < sp.length; i++) {
              E.info("pt " + i + " " + sp[i].nnbr);
           }
           */
    }



    public TreePoint[] getFixedWidthSlices(double dx, HashMap<String, Double> resHM) {
        sdstyle = FIXED;

        resolution = new Resolution(dx, resHM);

        maxnp = 20000; // should know what your doing if set dx;
        discretize();
        return getSlices();
    }


    public TreePoint[] getBalancedSlices(double disqrtr, int mnp) {
        sdstyle = BALANCED;
        resolution = new Resolution(disqrtr, null);
        maxnp = mnp;
        discretize();
        return getSlices();
    }


    private TreePoint[] getSlices() {
        return outPoints;
    }

    private void discretize() {
        int np = srcPoints.length;

        double[][][] subdiv = new double[np][6][];
        for (int i = 0; i < np; i++) {
            srcPoints[i].setWork(i);
        }

        int nnp = 0;
        for (int i = 0; i < np; i++) {
            TreePoint cpa = srcPoints[i];
            for (int j = 0; j < cpa.nnbr; j++) {
                TreePoint cpb = cpa.nbr[j];
                if (cpa.getWork() < cpb.getWork()) {

                    if (sdstyle == FIXED) {
                        /*
                        subdiv[i][j] = getFixedSubdivision(cpa, cpb);
                        */
                        //<--WK (1)moved the body of getFixedSubdivision(...) here,
                        //(2) added the 'distance_over_delta' variable, and
                        //(3) modified the 'nadd' variable
                        double dab = cpa.distanceTo(cpb);
                        double localDelta = resolution.getLocalDelta(cpa, cpb);

                        double distance_over_delta = dab/localDelta; //wk
                        int nadd = (int)distance_over_delta - 1; //wk

                        // RCC  - can resolve bug 2 (23 Feb 09) with round as in
                        // original - not sure what else it changes
                        // 	   int nadd = (int)Math.round(distance_over_delta) - 1;
                        /*
                        int nadd = (int)(Math.round(dab / localDelta)) - 1;
                         */



                        if (nadd < 0) {
                            if (dab < 0.01) {
                                // presumably cpb is the start point of a new
                                // segment positioned at cpa but with a different radius
                                cpb.setSubAreaOf(cpa);

                                cpb.alignTop(cpa, cpa.largestNeighborNot(cpb), cpa.partBranchOffset);
                                cpa.partBranchOffset += 2 * cpb.r;

                            } else
                                log.warn("Distance between points is smaller than the desired element size {} {} {} {}",
                                         dab, localDelta, cpa, cpb);

                            nadd = 0;
                        }


                        double[] dpos = new double[nadd];
                        if (nadd > 0) {
                            for (int ii = 0; ii < nadd; ii++) {
                                dpos[ii] = (1. + ii) / (nadd + 1.);
                            }
                        }
                        /*
                        return dpos;
                        */
                        subdiv[i][j] = dpos;
                        //WK-->

                        //<--WK: change the coordinates of cpb to make all volume elements
                        //be localDelta-by-localDelta-squares.  The distance between cpa and modified cpb
                        //now becomes localDelta*(floor(dab/localDelta)).

                        /*
                        // RCC temporarily removed - for daughter branches, the daughter
                        // points would have to be moved too or you get extra segments you
                        // don't want

                        TreePoint cp = new TreePoint();
                        cp.locateBetween(cpa, cpb, (nadd+1)/distance_over_delta);
                        cpb.x = cp.x;
                        cpb.y = cp.y;
                        cpb.z = cp.z;
                        */


                        //System.out.println("start and end points " + cpa.x + " " + cpa.y + " " + cpb.x + " " + cpb.y); //wk
                        //System.out.println("distance delta nadd " + dab + " " + localDelta + " " + nadd); //wk
                        //WK-->


                    } else {
                        assert sdstyle == BALANCED: sdstyle;
                        subdiv[i][j] = getBalancedSubdivision(cpa, cpb);
                    }
                    nnp += subdiv[i][j].length;
                }
            }
        }

        if (np + nnp > maxnp) {
            log.error("not discretizing: need too many points ({})", np + nnp);
            throw new RuntimeException("need too many points");
        }

        TreePoint[] pr = new TreePoint[np + nnp];
        for (int i = 0; i < np; i++) {
            pr[i] = srcPoints[i];
            // E.info("src point " + i + " " + pr[i].getLabel());
        }
        int nxp = np;

        for (int i = 0; i < np; i++) {
            TreePoint cpa = srcPoints[i];
            for (int j = 0; j < cpa.nnbr; j++) {
                double[] div = subdiv[i][j];

                if (div != null && div.length > 0) {
                    TreePoint cpb = cpa.nbr[j];

                    String newRegion = cpa.regionClassWith(cpb);
                    String newID = cpa.segmentIDWith(cpb);


                    TreePoint clast = cpa;
                    for (int id = 0; id < div.length; id++) {
                        TreePoint cp = new TreePoint();
                        cp.locateBetween(cpa, cpb, div[id]);
                        cp.addNeighbor(clast);

                        pr[nxp++] = cp;
                        if (id == 0) {
                            cpa.replaceNeighbor(cpb, cp);
                        } else {
                            clast.addNeighbor(cp);
                        }


                        if (id == div.length - 1) {
                            cpb.replaceNeighbor(cpa, cp);
                            cp.addNeighbor(cpb);
                            cp.setIDWith(cpb, newID);
                            cp.setRegionWith(cpb, newRegion);
                        }

                        clast.setIDWith(cp, newID);
                        clast.setRegionWith(cp, newRegion);
                        cp.setIDWith(clast, newID);
                        cp.setRegionWith(clast, newRegion);

                        clast = cp;
                    }
                }
            }
        }

        // now patch in the offsetChildren to the nearest point in the
        // new discretization;


        ArrayList<TreePoint[]> cns = new ArrayList<TreePoint[]>();

        for (int i = 0; i < np; i++) {
            TreePoint cpa = srcPoints[i];
            if (cpa.hasOffsetChildren()) {
                for (TreePoint tpoc : cpa.getOffsetChildren()) {
                    TreePoint tpn = findNearest(cpa, tpoc);
                    if (tpn != null) {
                        TreePoint[] tpa = {tpn, tpoc};
                        cns.add(tpa);
                    }
                }
            }
        }

        // don't do them in the loop above because it could mess up the search for
        // later ones
        for (TreePoint[] tpa : cns) {
            log.info("Patching in {} as child of nearest in parent segment: {}",
                     tpa[1], tpa[0]);
            TreePoint.neighborize(tpa[0], tpa[1]);
        }
        outPoints = pr;
    }





    private double[] getFixedSubdivision(TreePoint cpa, TreePoint cpb) {
        double dab = cpa.distanceTo(cpb);

        double localDelta = resolution.getLocalDelta(cpa, cpb);

        int nadd = (int)(Math.round(dab / localDelta)) - 1;

        double[] dpos = new double[nadd];
        if (nadd > 0) {
            for (int i = 0; i < nadd; i++) {
                dpos[i] = (1. + i) / (nadd + 1.);
            }
        }

        return dpos;
    }



    private double[] getBalancedSubdivision(TreePoint cpa, TreePoint cpb) {
        double dab = cpa.distanceTo(cpb);
        double ra = cpa.r;
        double rb = cpb.r;

        double localDelta = resolution.getLocalDelta(cpa, cpb);

        double fdist = 0.0;
        // fdist is to be the integral in question between pta and ptb;
        if (rb != ra) {
            fdist = ((2. / 3.) * dab / (rb - ra) * (Math.pow(rb, 3. / 2.) - Math.pow(ra, 3. / 2.)));
            // lbya = dab * (1./rb - 1./ra) / (Math.PI * (ra - rb));

        } else {
            fdist = dab * Math.sqrt(ra);
            // lbya = dab / (Math.PI * ra * ra);
        }
        // aseg = dab * Math.PI * (ra + rb);

        int nadd = (int)(fdist / localDelta);

        double[] dpos = new double[nadd];

        if (nadd > 0) {

            if (Math.abs((ra - rb) / (ra + rb)) < 0.01) {
                for (int i = 0; i < nadd; i++) {
                    dpos[i] = (1. + i) / (nadd + 1.);
                }
            } else {
                // chop up the carrot;
                double delf = fdist / (nadd + 1);
                double ffa = (rb - ra) / dab; // dr/dx
                double xa = ra / ffa;
                double xb = rb / ffa;
                // xa and xb are the end positions measured from where
                // the carrot comes to a point.
                double x = xa;

                // the integral of sqrt(r) dx is
                // 2/3 * dx / (rb-ra) * (rb^3/2 - ra^3/2)
                // so need dx such that this is delf (= total_int / nseg)

                for (int i = 0; i < nadd + 1; i++) {
                    double ttt = (delf * ffa * 3. / 2. + Math.pow(ffa * x, 3. / 2.));
                    double dx = Math.pow(ttt, (2. / 3.)) / ffa - x;
                    x += dx;
                    if (i < nadd) {
                        dpos[i] = (x - xa) / dab;
                    }
                }
                if (Math.abs(xb - x) > 1.e-5)
                    log.warn("segment division " + xa + " " + xb + " " + x + " " + nadd + " " + dab + " "
                             + ra + " " + rb);
            }
        }
        return dpos;
    }


    public TreePoint findNearest(TreePoint rt, TreePoint tgt) {
        TreePoint ret = rt;
        double dg = rt.distanceTo(tgt);

        // only follow the segments from rt to their first branch point;
        for (int i = 0; i < rt.nnbr; i++) {
            double ds = rt.distanceTo(tgt);
            TreePoint tpp = rt;
            TreePoint tpn = rt.nbr[i];

            while (tpn != null && tpn.distanceTo(tgt) < ds) {
                ds = tpn.distanceTo(tgt);
                TreePoint tpprev = tpp;
                tpp = tpn;
                tpn = tpn.oppositeNeighbor(tpprev);

            }
            if (ds < dg) {
                ret = tpp;
                dg = ds;
            }
        }
        return ret;
    }



}
