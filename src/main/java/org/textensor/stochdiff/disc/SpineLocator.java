package org.textensor.stochdiff.disc;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.*;
import org.textensor.stochdiff.geom.Geom;
import org.textensor.stochdiff.geom.Position;
import org.textensor.stochdiff.geom.Vector;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.math.RandomMath;
import org.textensor.stochdiff.numeric.morph.*;
import org.textensor.util.ArrayUtil;

import java.util.HashSet;

import java.util.HashMap;

public class SpineLocator {

    long spineSeed;
    SpineDistribution spineDist;
    double spineDX;

    MersenneTwister rngen;

    HashMap<SpineProfile, double[][]> profHM;

    public SpineLocator(int seed, SpineDistribution sd, double delta) {
        spineSeed = seed;
        spineDist = sd;
        spineDX = delta;

        if (spineSeed <= 0) {
            spineSeed = (long)(1.e5 * Math.random());
        }
        rngen = new MersenneTwister();
        rngen.setSeed(spineSeed);
    }




    public void addSpinesTo(VolumeGrid volumeGrid) {


        for (SpinePopulation sp : spineDist.getPopulations()) {

            double density = sp.getDensity();
            String reg = sp.getTargetRegion();

            ArrayList<VolumeElement> surfVE = new ArrayList<VolumeElement>();
            ArrayList<Double> surfA = new ArrayList<Double>();


            for (VolumeElement ve : volumeGrid.getElementsInRegion(reg)) {
                Position[] sbdry = ve.getSurfaceBoundary();
                if (sbdry != null) {
                    surfVE.add(ve);
                    surfA.add(new Double(Geom.getArea(sbdry)));
                }
            }


            if (surfA.size() <= 0) {
                E.warning("there no elements labelled with " + reg +
                          " but it is referenced from spine allocation");

            } else {
                double[] eltSA = new double[surfA.size()];

                double sum = 0.;
                for (int i = 0; i < eltSA.length; i++) {
                    sum += surfA.get(i).doubleValue();
                    eltSA[i] = sum;
                }


                for (int i = 1; i < eltSA.length; i++) {
                    eltSA[i] += eltSA[i-1];
                }

                double totalArea = eltSA[eltSA.length - 1];
                double avgNoSpines = totalArea * density;

                double nspines = RandomMath.poissonInt(avgNoSpines, rngen);

                if (nspines > 0.5 * eltSA.length) {
                    E.error("too many spines (need more than one per segment");
                    nspines = (int)(0.5 * eltSA.length);
                }

                HashSet<Integer> gotSpine = new HashSet<Integer>();
                int ndone = 0;

                if (avgNoSpines > 0. && nspines == 0) {
                    E.info("spines : although the density is non-zero, random allocation " +
                           "gives no spines for region " + reg + " (avg=" + avgNoSpines + ")");
                }

                while (ndone < nspines) {
                    double abelow = rngen.random() * totalArea;
                    int posInArray = ArrayUtil.findBracket(eltSA, abelow);

                    if (posInArray < 0) {
                        E.info("tot area " + totalArea);
                        E.dump("cant get pos " + abelow,  eltSA);
                    }

                    Integer ip = new Integer(posInArray);
                    if (gotSpine.contains(ip)) {
                        // already got a spine - go round again;
                    } else {
                        gotSpine.add(ip);

                        ArrayList<VolumeElement> elts = addSpineTo(surfVE.get(posInArray), sp.getProfile());
                        volumeGrid.addElements(elts);
                        ndone += 1;
                    }
                }
            }
        }
    }


    private ArrayList<VolumeElement> addSpineTo(VolumeElement vedend, SpineProfile prof) {
        Position[] perim = vedend.getSurfaceBoundary();
        Vector vnorm = Geom.getUnitNormal(perim);
        Position pcen = Geom.cog(perim);

        double[][] xw = getBoundaryWidths(prof, spineDX);

        double[] xp = xw[0];
        double[] wb = xw[1];
        double[] rb = new double[wb.length];
        for (int i = 0; i < wb.length; i++) {
            rb[i] = 0.5 * wb[i];
        }

        ArrayList<VolumeElement> ret = new ArrayList<VolumeElement>();

        Translation trans = Geom.translation(pcen);
        double theta = Geom.zRotationAngle(Geom.unitX(), vnorm);
        Rotation rot = Geom.aboutZRotation(theta);

        VolumeElement vprev = vedend;

        for (int i = 0; i < xp.length-1; i++) {
            double dx = xp[i+1] - xp[i];
            double vol = Math.PI * dx * (rb[i]*rb[i] + rb[i+1]*rb[i+1] + rb[i]*rb[i+1]) / 3.;

            double baseArea = Math.PI * (rb[i] * rb[i]);


            VolumeElement ve = new VolumeElement();

            Position cp = Geom.position(0.5 * (xp[i] + xp[i+1]), 0., 0.);
            Position pr = rot.getRotatedPosition(cp);
            Position pc = trans.getTranslated(pr);
            ve.setCenterPosition(pc.getX(), pc.getY(), pc.getZ());

            Position[] pbdry = {Geom.position(xp[i+1], rb[i+1], 0),
                                Geom.position(xp[i], rb[i], 0),
                                Geom.position(xp[i], -rb[i], 0),
                                Geom.position(xp[i+1], -rb[i+1], 0)
                               };

            for (int ib = 0; ib < pbdry.length; ib++) {
                pbdry[ib] = trans.getTranslated(rot.getRotatedPosition(pbdry[ib]));
            }

            ve.setBoundary(pbdry);
            ve.setVolume(vol);

            vprev.coupleTo(ve, baseArea);
            ret.add(ve);
            /*
               if (regionLabel != null) {
                  ve.setRegion(regionLabel);
               }
             */
            vprev = ve;
        }
        return ret;
    }





    private double[][] getBoundaryWidths(SpineProfile sp, double dx) {
        if (profHM == null) {
            profHM = new HashMap<SpineProfile, double[][]>();
        }
        double[][] ret = null;
        if (profHM.containsKey(sp)) {
            ret = profHM.get(sp);

        } else {
            double[] ax = sp.getXPts();
            double[] aw = sp.getWidths();
            double ltot = ax[ax.length-1];
            int nel = (int)(ltot / dx + 0.5);
            if (nel < 1) {
                nel = 1;
            }

            double[] xbd = ArrayUtil.span(0., ltot, nel);
            double[] wv = ArrayUtil.interpInAtFor(aw, ax, xbd);

            ret = new double[2][];
            ret[0] = xbd;
            ret[1] = wv;
        }

        return ret;
    }




}
