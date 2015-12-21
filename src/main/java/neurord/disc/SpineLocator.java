package neurord.disc;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;

import neurord.geom.*;
import neurord.numeric.math.MersenneTwister;
import neurord.numeric.math.RandomMath;
import neurord.numeric.morph.*;
import neurord.util.ArrayUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class SpineLocator {
    static final Logger log = LogManager.getLogger();

    public static void locate(int seed, SpineDistribution dist, double delta, VolumeGrid grid) {

        // FIXME: replace by random generator wrapper
        final MersenneTwister rngen = new MersenneTwister();
        rngen.setSeed(seed > 0 ? seed : Math.abs(new Random().nextLong()));

        final HashMap<SpineProfile, DiscretizedSpine> spines = new HashMap<>();
        final HashSet<VolumeElement> volumes = new HashSet<>();

        int nblocked = 0;

        int ipop = 0;
        for (SpinePopulation sp : dist.getPopulations()) {
            ipop += 1;

            String popid = sp.getID();
            if (popid == null)
                popid = "";

            final double density = sp.getDensity();
            final String reg = sp.getTargetRegion();

            log.info("Allocating spines in popid=\"{}\" region=\"{}\"",
                     popid, reg);

            final ArrayList<VolumeElement> surfVE = new ArrayList<>();
            final ArrayList<Double> surfA = new ArrayList<>();

            for (VolumeElement ve : grid.getElementsInRegion(reg)) {
                Position[] sbdry = ve.getSurfaceBoundary();
                if (sbdry != null) {
                    surfVE.add(ve);
                    surfA.add(new Double(ve.getExposedArea()));
                }
            }

            if (surfA.isEmpty()) {
                log.warn("There surface elements labelled \"{}\" found");
                throw new RuntimeException("SpineAllocation references surface-less region");
            }

            final double[] eltSA = new double[surfA.size()];

            double sum = 0.;
            for (int i = 0; i < eltSA.length; i++) {
                sum += surfA.get(i).doubleValue();
                eltSA[i] = sum;
            }


            double totalArea = eltSA[eltSA.length - 1];
            double avgNoSpines = totalArea * density;

            // double nspines = RandomMath.poissonInt(avgNoSpines, rngen);
            double nspines = avgNoSpines;

            // the above might take a variable number of random nos off
            // rngen
            // for certain small variations in avgNoSpines so reseed rngen
            // now
            // to get reliable spine position repeats;

            rngen.setSeed(seed + ipop);

            /*			****		AB: 0.5 produces too few spines
                        if (nspines > 0.5 * eltSA.length) {
                        E.error("too many spines (need more than one per segment");
                        nspines = (int) (0.5 * eltSA.length);
                        }
            */
            if (nspines > eltSA.length) {
                log.warn("too many spines (need more than one per segment)");
                nspines = (int)(eltSA.length);
            }

            log.info("Surface area for spine group \"{}\" on \"{}\" is {} (nspines={})",
                     popid, reg, sum, nspines);

            int ndone = 0;

            if (avgNoSpines > 0 && nspines == 0)
                log.info("spines : although the density is non-zero, random allocation "
                         + "gives no spines for region \"{}\" (avg={})", reg, avgNoSpines);

            ArrayList<Integer> positionA = new ArrayList<Integer>();
            while (ndone < nspines) {
                double abelow = rngen.random() * totalArea;
                int posInArray = ArrayUtil.findBracket(eltSA, abelow);

                if (posInArray < 0) {
                    log.info("Total area: {}", totalArea);
                    log.error("Cannot get pos {}. {}", abelow, eltSA);
                    throw new RuntimeException("Cannot get pos " + abelow);
                }

                if (volumes.contains(surfVE.get(posInArray))) {
                    // already got a spine - go round again;
                    nblocked += 1;

                } else {
                    positionA.add(posInArray);
                    ndone += 1;
                }
                if (nblocked > 100) {
                    throw new RuntimeException("Can't fine any vacant elements to add a spine to");
                }
            }
            Collections.sort(positionA);

            ndone = 0;
            for (int posInArray : positionA) {
                List<VolumeElement> elts = addSpineTo(spines, delta,
                                                      surfVE.get(posInArray),
                                                      sp.getProfile(), popid, ndone);
                grid.addElements(elts);
                ndone += 1;
            }
        }
    }

    private static ArrayList<VolumeElement> addSpineTo(HashMap<SpineProfile, DiscretizedSpine> spines,
                                                       double delta,
                                                       VolumeElement vedend,
                                                       SpineProfile prof, String popid, int idx) {

        Position[] perim = vedend.getSurfaceBoundary();

        Vector vnorm = Geom.getUnitNormal(perim);
        Position pcen = Geom.cog(perim);

        DiscretizedSpine xw = getBoundaryWidths(spines, prof, delta);

        double[] xp = xw.getBoundaries();
        double[] wb = xw.getWidths();
        String[] lbls = xw.getLabels();
        String[] rgns = xw.getRegions();

        double[] rb = new double[wb.length];
        for (int i = 0; i < wb.length; i++) {
            rb[i] = 0.5 * wb[i];
        }

        // E.info("vnomr is " + vnorm);

        ArrayList<VolumeElement> ret = new ArrayList<VolumeElement>();

        Translation trans = Geom.translation(pcen);

        double aroty = -Geom.zElevation(vnorm);

        double arotz = Geom.xzRotationAngle(vnorm);

        GRotation rotx = Geom.aboutYRotation(aroty);
        GRotation rotz = Geom.aboutZRotation(arotz);
        Rotation rot = rotz.times(rotx);

        Vector vx = Geom.unitX();
        Position prot = rot.getRotatedPosition(Geom.endPosition(vx));
        double da = Geom.angleBetween(vnorm, Geom.getToVector(prot));

        if (Math.abs(da) > 1.e-6)
            throw new RuntimeException("rotation angle miscalculation: residual angle is " + da);


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


            VolumeElement ve = null;

            Position[] pbdry = { Geom.position(xp[i + 1], rb[i + 1], 0), Geom.position(xp[i], rb[i], 0),
                                 Geom.position(xp[i], -rb[i], 0), Geom.position(xp[i + 1], -rb[i + 1], 0)
                               };

            final String lroot = popid + "[" + idx + "]";
            final String label = lbls[i] != null ? lroot + "." + lbls[i] : null;
            final String groupID = lbls[i] == null ? lroot : null;
            final double deltaZ = 0.5 * (rb[i] + rb[i + 1]);

            for (int j = 0; j < pbdry.length; j++)
                pbdry[j] = trans.getTranslated(rot.getRotatedPosition(pbdry[j]));

            final Position
                cp = Geom.position(0.5 * (xp[i] + xp[i + 1]), 0., 0.),
                pr = rot.getRotatedPosition(cp),
                center = trans.getTranslated(pr);

            if (vedend instanceof CuboidVolumeElement) {
                ve = new CuboidVolumeElement(label, rgns[i], groupID,
                                             pbdry,
                                             null, 0.0,
                                             center,
                                             0.0, 0.0, 0.0,
                                             vol, deltaZ);
            } else if (vedend instanceof CurvedVolumeElement) {
                CurvedVolumeElement cve = new CurvedVolumeElement(label, rgns[i], groupID,
                                                                  pbdry,
                                                                  null, 0.0,
                                                                  center,
                                                                  vol, deltaZ);
                ve = cve;
                TrianglesSet ts = makeTriangles(xp[i], xp[i+1], rb[i], rb[i+1]);
                ts.rotate(rot);
                ts.translate(trans);

                cve.setTriangles(ts.getStripLengths(), ts.getPositions(), ts.getNormals());
            } else
                throw new RuntimeException("unknown element type " + vedend);

            vprev.coupleTo(ve, baseArea);
            ret.add(ve);

            vprev = ve;
        }
        return ret;
    }

    private static DiscretizedSpine getBoundaryWidths(HashMap<SpineProfile, DiscretizedSpine> spines,
                                                      SpineProfile sp, double dx)
    {
        DiscretizedSpine ret = spines.get(sp);
        if (ret != null)
            return ret;

        double[] ax = sp.getXPts();
        double[] aw = sp.getWidths();
        String[] pl = sp.getLabels();
        String[] prl = sp.getRegions();

        double ltot = ax[ax.length - 1];
        int nel = (int)(ltot / dx + 0.5);
        if (nel < 1)
            nel = 1;

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

        return new DiscretizedSpine(xbd, wv, lbls, rgns);
    }

    private static TrianglesSet makeTriangles(double xa, double xb,
                                              double ra, double rb)
    {
        // spines are built lying along the x axis

        TrianglesSet ret = new TrianglesSet();
        int nseg = 12;

        ret.add(makeEnd(xa, ra, nseg, -1));
        ret.add(makeEnd(xb, rb, nseg, 1));
        ret.add(makeOuter(xa, xb, ra, rb, nseg, -1));

        return ret;
    }

    private static TriangleStrip makeEnd(double xa, double ra,
                                         int nseg, int idir)
    {
        TriangleStrip ret = new TriangleStrip();
        double eps = 1.e-6;
        for (int i = 0; i < nseg + 1; i++) {
            double theta = (2. * Math.PI * i) / nseg;
            double ct = Math.cos(theta);
            double st = Math.sin(theta);

            ret.addPoint(xa, eps * ct, eps * st, idir, 0, 0);
            ret.addPoint(xa, ra * ct, ra * st, idir, 0, 0);
        }

        if (idir < 0)
            ret.flip();

        return ret;
    }

    private static TriangleStrip makeOuter(double xa, double xb,
                                           double ra, double rb,
                                           int nseg, int idir)
    {
        TriangleStrip ret = new TriangleStrip();

        double ayz = Math.atan2(rb - ra, xb - xa);
        double fyz = Math.sin(ayz);
        double fx = Math.cos(ayz);

        for (int i = 0; i < nseg + 1; i++) {
            double a = (2.0 * Math.PI * i) / nseg;

            double ca = Math.cos(a);
            double sa = Math.sin(a);

            double xn = fyz * idir;  // TODO check sign
            double yn = -fx * idir * ca;
            double zn = -fx * idir * sa;

            ret.addPoint(xa, ra * ca, ra* sa,  xn, yn, zn);
            ret.addPoint(xb, rb * ca, rb* sa,  xn, yn, zn);
        }

        if (idir < 0)
            ret.flip();

        return ret;
    }
}
