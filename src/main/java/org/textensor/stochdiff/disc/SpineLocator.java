package org.textensor.stochdiff.disc;

import java.util.ArrayList;
import java.util.Collections;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.*;
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

    HashMap<SpineProfile, DiscretizedSpine> profHM;

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

            String popid = sp.getID();
            if (popid == null) {
                popid = "";
            }

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
                E.warning("there no elements labelled with " + reg + " but it is referenced from spine allocation");

            } else {
                double[] eltSA = new double[surfA.size()];

                double sum = 0.;
                for (int i = 0; i < eltSA.length; i++) {
                    sum += surfA.get(i).doubleValue();
                    eltSA[i] = sum;
                }

                E.info("total surface area for spine group  " + popid + " on " + reg + " is " + sum);

                double totalArea = eltSA[eltSA.length - 1];
                double avgNoSpines = totalArea * density;

                // double nspines = RandomMath.poissonInt(avgNoSpines, rngen);
                double nspines = avgNoSpines;

                // the above might take a variable number of random nos off
                // rngen
                // for certain small variations in avgNoSpines so reseed rngen
                // now
                // to get reliable spine position repeats;

                rngen.setSeed(spineSeed);

                if (nspines > 0.5 * eltSA.length) {
                    E.error("too many spines (need more than one per segment");
                    nspines = (int)(0.5 * eltSA.length);
                }

                HashSet<Integer> gotSpine = new HashSet<Integer>();
                int ndone = 0;

                if (avgNoSpines > 0. && nspines == 0) {
                    E.info("spines : although the density is non-zero, random allocation "
                           + "gives no spines for region " + reg + " (avg=" + avgNoSpines + ")");
                }

                ArrayList<Integer> positionA = new ArrayList<Integer>();
                while (ndone < nspines) {
                    double abelow = rngen.random() * totalArea;
                    int posInArray = ArrayUtil.findBracket(eltSA, abelow);

                    if (posInArray < 0) {
                        E.info("tot area " + totalArea);
                        E.dump("cant get pos " + abelow, eltSA);
                    }

                    Integer ip = new Integer(posInArray);
                    if (gotSpine.contains(ip)) {
                        // already got a spine - go round again;
                    } else {
                        gotSpine.add(ip);
                        positionA.add(ip);
                        ndone += 1;
                    }
                }
                Collections.sort(positionA);

                for (int posInArray : positionA) {
                    ArrayList<VolumeElement> elts = addSpineTo(surfVE.get(posInArray), sp.getProfile(), popid,
                                                    ndone);
                    volumeGrid.addElements(elts);
                }
            }
        }
    }

    private ArrayList<VolumeElement> addSpineTo(VolumeElement vedend, SpineProfile prof, String popid, int idx) {
        Position[] perim = vedend.getSurfaceBoundary();
        Vector vnorm = Geom.getUnitNormal(perim);
        Position pcen = Geom.cog(perim);

        DiscretizedSpine xw = getBoundaryWidths(prof, spineDX);

        double[] xp = xw.getBoundaries();
        double[] wb = xw.getWidths();
        String[] lbls = xw.getLabels();
        String[] rgns = xw.getRegions();

        double[] rb = new double[wb.length];
        for (int i = 0; i < wb.length; i++) {
            rb[i] = 0.5 * wb[i];
        }

        ArrayList<VolumeElement> ret = new ArrayList<VolumeElement>();

        Translation trans = Geom.translation(pcen);
        double theta = Geom.zRotationAngle(Geom.unitX(), vnorm);
        Rotation rot = Geom.aboutZRotation(theta);

        VolumeElement vprev = vedend;

        for (int i = 0; i < xp.length - 1; i++) {
            double dx = xp[i + 1] - xp[i];
            double vol = Math.PI * dx * (rb[i] * rb[i] + rb[i + 1] * rb[i + 1] + rb[i] * rb[i + 1]) / 3.;

            /*
             * The above gives the volume of a fustrum radius rb[i] at one end
             * and rb[i+1] at the other: calling the radii a and b, and scaling
             * x to 1, then the volume is integral_0,1 pi (a + (b-a)x)^2 dx = PI
             * * integral_0,1 a^2 + 2 a (b-a)x + (b-a)^2 x^2 = pi * (a^2 + 2 (a
             * b - a^2)/2 + (b^2 + a^2 - 2ab)/3 = pi * (a*2 + b^2 + a b) / 3
             */

            double baseArea = Math.PI * (rb[i] * rb[i]);

            CuboidVolumeElement ve = new CuboidVolumeElement();

            Position cp = Geom.position(0.5 * (xp[i] + xp[i + 1]), 0., 0.);
            Position pr = rot.getRotatedPosition(cp);
            Position pc = trans.getTranslated(pr);
            ve.setCenterPosition(pc.getX(), pc.getY(), pc.getZ());

            Position[] pbdry = { Geom.position(xp[i + 1], rb[i + 1], 0), Geom.position(xp[i], rb[i], 0),
                                 Geom.position(xp[i], -rb[i], 0), Geom.position(xp[i + 1], -rb[i + 1], 0)
                               };

            for (int ib = 0; ib < pbdry.length; ib++) {
                pbdry[ib] = trans.getTranslated(rot.getRotatedPosition(pbdry[ib]));
            }

            ve.setBoundary(pbdry);
            ve.setVolume(vol);
            ve.setDeltaZ(0.5 * (rb[i] + rb[i + 1]));

            vprev.coupleTo(ve, baseArea);
            ret.add(ve);
            String lroot = popid + "[" + idx + "]";
            if (lbls[i] != null) {
                String ll = lroot + "." + lbls[i];
                ve.setLabel(ll);
            } else {
                ve.setGroupID(lroot);
            }
            if (rgns[i] != null) {
                ve.setRegion(rgns[i]);
            }

            vprev = ve;
        }
        return ret;
    }

    private DiscretizedSpine getBoundaryWidths(SpineProfile sp, double dx) {
        if (profHM == null) {
            profHM = new HashMap<SpineProfile, DiscretizedSpine>();
        }
        DiscretizedSpine ret = null;
        if (profHM.containsKey(sp)) {
            ret = profHM.get(sp);

        } else {
            double[] ax = sp.getXPts();
            double[] aw = sp.getWidths();
            String[] pl = sp.getLabels();
            String[] prl = sp.getRegions();

            double ltot = ax[ax.length - 1];
            int nel = (int)(ltot / dx + 0.5);
            if (nel < 1) {
                nel = 1;
            }

            double[] xbd = ArrayUtil.span(0., ltot, nel);
            double[] wv = ArrayUtil.interpInAtFor(aw, ax, xbd);

            String[] lbls = new String[nel];
            String[] rgns = new String[nel];
            int ipr = 0;

            for (int i = 0; i < nel; i++) {
                while (ipr < ax.length - 2 && ax[ipr + 1] < xbd[i]) {
                    ipr += 1;
                }
                double db = xbd[i] - ax[ipr];
                double df = ax[ipr + 1] - xbd[i];

                if (db < df) {
                    rgns[i] = prl[ipr];
                } else {
                    rgns[i] = prl[ipr + 1];
                }
            }

            for (int i = 0; i < ax.length; i++) {
                if (pl[i] != null) {
                    double dmin = 1.e6;
                    int imin = 0;
                    for (int j = 0; j < nel; j++) {
                        double d = Math.abs(xbd[j] - ax[i]);
                        if (d < dmin) {
                            dmin = d;
                            imin = j;
                        }
                    }
                    lbls[imin] = pl[i];
                }
            }

            ret = new DiscretizedSpine(xbd, wv, lbls, rgns);
        }

        return ret;
    }

}
