package neurord.disc;

import neurord.geom.*;
import neurord.numeric.morph.CurvedVolumeElement;
import neurord.numeric.morph.TreePoint;
import neurord.numeric.morph.TriangleStrip;
import neurord.numeric.morph.TrianglesSet;

import java.util.ArrayList;
import java.util.HashMap;

public class CurvedVolumeSlice {

    double baseDelta;
    double radiusa;
    double radiusb;


    double[] bdsa;
    double[] bdsb;
    int[] nazim;

    ArrayList<CurvedVolumeElement> elements;
    HashMap<Integer, ArrayList<CurvedVolumeElement>> radHM;

    double maxAspectRatio = 2;

    public CurvedVolumeSlice(double delta, double ra, double rb) {
        baseDelta = delta;
        radiusa = ra;
        radiusb = rb;
    }


    public double[] getRadii(int end) {
        return (end == 0 ? bdsa : bdsb);
    }

    public int[] getNazimuthals() {
        return nazim;
    }


    public void discFill(Position pa, Position pb, String pointLabel, String regionLabel,
                         double[] surfaceLayers, double maxAR) {
        maxAspectRatio = maxAR;

        double axlen = Geom.distanceBetween(pa, pb);
        Translation trans = Geom.translation(Geom.midpoint(pa, pb));
        Vector vab = Geom.fromToVector(pa, pb);
        double arotx = Geom.zElevation(vab);
        double arotz = Geom.yzRotationAngle(vab);
        GRotation rotx = Geom.aboutXRotation(arotx);
        GRotation rotz = Geom.aboutZRotation(arotz);
        Rotation rot = rotz.times(rotx);

        Vector vy = Geom.unitY();
        Position prot = rot.getRotatedPosition(Geom.endPosition(vy));


        // Position prwk = rotx.getRotatedPosition(Geom.endPosition(vy));
        // Position prot = rotz.getRotatedPosition(prwk);

        double da = Geom.angleBetween(vab, Geom.getToVector(prot));


        if (Math.abs(da) > 1.e-6) {
            throw new RuntimeException("rotation angle miscalculation: residual angle is " + da);
        }


        elements = new ArrayList<>();


        // center of the box at 0,0


        // this is a little confusing. X and Y axes are used within the slice, but when these are
        // turned into boxes, the slab of boxes is initially created in the X-Z plane before being rotated
        // into place

        double maxr = Math.max(radiusa, radiusb);
        double[] bdm = getRadialSplit(maxr, surfaceLayers);

        bdsa = getRadialSplit(radiusa, bdm.length, surfaceLayers);
        bdsb = getRadialSplit(radiusb, bdm.length, surfaceLayers);


        nazim = getAzimuthalSplits(maxr, bdm);

        radHM = new HashMap<Integer, ArrayList<CurvedVolumeElement>>();

        for (int ir = 0; ir < bdsa.length; ir++) {


            double ra1 = (ir > 0 ? bdsa[ir-1] : 0);
            double ra2 = bdsa[ir];

            double rb1 = (ir > 0 ? bdsb[ir-1] : 0);
            double rb2 = bdsb[ir];

            double rc = ((ra1 + ra2) / 2 + (rb1 + rb2) / 2) / 2;


            int na = nazim[ir];
            double eltangle = 2. * Math.PI / na;

            double volouter = axlen * (ra2 * ra2 + rb2 * rb2 + ra2 * rb2) / 3.;
            double volinner = axlen * (ra1 * ra1 + rb1 * rb1 + ra1 * rb1) / 3.;
            double eltvol = (volouter - volinner) / na;

            double surfouter = 2 * Math.PI * (ra2 + rb2) / 2.;

            double carea = axlen * ((ra2 - ra1) + (rb2 - rb1)) / 2;

            double subarea = (axlen * 2 * Math.PI * (ra1 + rb1) / 2) / na;



            ArrayList<CurvedVolumeElement> azb = null;
            double eltangleb = 1.;
            if (ir > 0) {
                azb = radHM.get(ir-1);
                eltangleb = 2 * Math.PI / azb.size();
            }


            ArrayList<CurvedVolumeElement> az = new ArrayList<>();
            for (int ia = 0; ia < na; ia++) {

                final double theta = ia * eltangle;

                final double thetaC = theta + 0.5 * eltangle;
                final double vcx = rc * Math.cos(thetaC);
                final double vcy = 0.;
                final double vcz = rc * Math.sin(thetaC);
                final Position center = trans.getTranslated(
                                            rot.getRotatedPosition(
                                                Geom.position(vcx, vcy, vcz)));

                final double rca = (ra1 + ra2) / 2;
                final double rcb = (rb1 + rb2) / 2;

                final double dtheta = Math.max(eltangle / 2, 1);
                final double ha = 0.5 * axlen;

                final Position[] pbdry = new Position[] {
                    Geom.position(rca * Math.cos(thetaC - dtheta), -ha, rca * Math.sin(thetaC - dtheta)),
                    Geom.position(rcb * Math.cos(thetaC - dtheta), ha, rcb * Math.sin(thetaC - dtheta)),
                    Geom.position(rcb * Math.cos(thetaC + dtheta), ha, rcb * Math.sin(thetaC + dtheta)),
                    Geom.position(rca * Math.cos(thetaC + dtheta), -ha, rca * Math.sin(thetaC + dtheta))
                };
                for (int ib = 0; ib < pbdry.length; ib++)
                    pbdry[ib] = trans.getTranslated(rot.getRotatedPosition(pbdry[ib]));


                final Position[] praw;
                if (ir == bdsa.length - 1) {
                    praw = new Position[] {
                        Geom.position(ra2 * Math.cos(thetaC - dtheta), -ha, ra2 * Math.sin(thetaC - dtheta)),
                        Geom.position(rb2 * Math.cos(thetaC - dtheta), ha, rb2 * Math.sin(thetaC - dtheta)),
                        Geom.position(rb2 * Math.cos(thetaC + dtheta), ha, rb2 * Math.sin(thetaC + dtheta)),
                        Geom.position(ra2 * Math.cos(thetaC + dtheta), -ha, ra2 * Math.sin(thetaC + dtheta))
                    };
                    for (int i = 0; i < praw.length; i++)
                        praw[i] = trans.getTranslated(rot.getRotatedPosition(praw[i]));
                } else
                    praw = null;

                CurvedVolumeElement ve = new CurvedVolumeElement(null, regionLabel, null,
                                                                 pbdry,
                                                                 praw,
                                                                 praw != null ? surfouter / na : 0.0,
                                                                 center,
                                                                 eltvol,  /* volume */
                                                                 0.0);    /* deltaZ */

                ve.setPositionIndexes(ir, ia);

                if (na == 2) {
                    if (ia == 1)
                        az.get(ia-1).coupleTo(ve, carea * 2);
                } else if (na > 2) {
                    if (ia > 0)
                        az.get(ia-1).coupleTo(ve, carea);

                    if (na > 2 && ia == na - 1)
                        ve.coupleTo(az.get(0), carea);
                }

                if (ir > 1) {
                    double thc = theta + 0.5 * eltangle;
                    int ib = (int)(thc / eltangleb);
                    azb.get(ib).coupleTo(ve, subarea);
                }

                final TrianglesSet ts = makeTriangles(axlen, ra1, ra2, rb1, rb2, theta, eltangle);
                ts.rotate(rot);
                ts.translate(trans);

                ve.setTriangles(ts.getStripLengths(), ts.getPositions(), ts.getNormals());

                az.add(ve);
                elements.add(ve);
            }
            radHM.put(ir, az);
        }
    }


    // TODO need a main method with some tests of getRadialSplit

    private TrianglesSet makeTriangles(double axlen, double ra1, double ra2, double rb1, double rb2, double theta,
                                       double eltangle) {

        // initial layout: elements are in the x-z plane, bottom surface at y = -0.5 * axlen, top at y = 0.5 * axlen
        // rotations measured up from the x axis,

        TrianglesSet ret = new TrianglesSet();

        double dth = Math.PI * 2. / 36.;
        int npart = (int)(Math.round(eltangle / dth));
        if (npart < 1) {
            npart = 1;
        }

        if (eltangle < 1.9 * Math.PI) {
            TriangleStrip tss = makeEnd(axlen, ra1, ra2, rb1, rb2, theta, -1);
            ret.add(tss);
            TriangleStrip tst = makeEnd(axlen, ra1, ra2, rb1, rb2, theta + eltangle, 1);
            ret.add(tst);
        }

        if (ra1 > 1.e-7) {
            TriangleStrip tsin = makeConeSurfacePart(axlen, ra1, rb1, theta, eltangle, -1, npart);
            ret.add(tsin);
        }
        TriangleStrip tsout = makeConeSurfacePart(axlen, ra2, rb2, theta, eltangle, 1, npart);
        ret.add(tsout);


        TriangleStrip tsp = makeSliceSurface(-0.5 * axlen, ra1, ra2, theta, eltangle, -1, npart);
        ret.add(tsp);

        TriangleStrip tsq = makeSliceSurface(0.5 * axlen, rb1, rb2, theta, eltangle, 1, npart);
        ret.add(tsq);

        return ret;
    }





    private TriangleStrip makeSliceSurface(double dy, double r1, double r2, double theta, double eltangle,
                                           int idir, int npart) {
        TriangleStrip ret = new TriangleStrip();

        double xn = 0.;
        double yn = idir;
        double zn = 0;

        for (int i = 0; i <= npart; i++) {
            double a = theta + i * eltangle / npart;
            double ca = Math.cos(a);
            double sa = Math.sin(a);
            ret.addPoint(r1 * ca, dy, r1 * sa, xn, yn, zn);
            ret.addPoint(r2 * ca, dy, r2 * sa, xn, yn, zn);

        }
        if (idir > 0) {
            ret.flip();
        }
        return ret;
    }



    private TriangleStrip makeConeSurfacePart(double axlen, double ra, double rb,
            double theta, double eltangle, int idir, int npart) {
        TriangleStrip ret = new TriangleStrip();



        double ay = Math.atan2(rb - ra, axlen);
        double fy = Math.sin(ay);
        double fr = Math.cos(ay);


        double am = -0.5 * axlen;
        double ap = 0.5 * axlen;

        for (int i = 0; i < npart + 1; i++) {
            double a = theta + i * eltangle / npart;

            double ca = Math.cos(a);
            double sa = Math.sin(a);
            double xn = fr * idir * ca;
            double yn = -fy * idir;  // TODO check sign
            double zn = fr * idir * sa;

            ret.addPoint(ra * ca, am, ra* sa,  xn, yn, zn);
            ret.addPoint(rb * ca, ap, rb* sa,  xn, yn, zn);
        }
        if (idir < 0) {
            ret.flip();
        }

        return ret;
    }



    private TriangleStrip makeEnd(double axlen, double ra1, double ra2, double rb1, double rb2, double theta, int idir) {
        TriangleStrip ret = new TriangleStrip();

        double y = -0.5 * axlen;
        double ct = Math.cos(theta);
        double st = Math.sin(theta);

        double xn = idir * st;
        double yn = 0.;
        double zn = -idir * ct;

        ret.addPoint(ra1 * ct, y, ra1 * st, xn, yn, zn);
        ret.addPoint(ra2 * ct, y, ra2 * st, xn, yn, zn);
        y = 0.5 * axlen;
        ret.addPoint(rb1 * ct, y, rb1 * st, xn, yn, zn);
        ret.addPoint(rb2 * ct, y, rb2 * st, zn, yn, zn);

        if (idir < 0) {
            ret.flip();
        }

        return ret;
    }




    private int[] getAzimuthalSplits(double radius, double[] bdm) {
        int[] ret = new int[bdm.length];
        int npre = 1;
        for (int i = 0; i < bdm.length; i++) {
            double rin = (i > 0 ? bdm[i-1] : 0);
            double rout = bdm[i];
            double rc = (rin + rout) / 2;

            double dr = rout - rin;
            double circ = 2 * Math.PI * rc;

            int nfac = (int)Math.round(Math.ceil((circ / (dr * maxAspectRatio)) / npre));
            ret[i] = npre * nfac;
            npre = ret[i];
        }
        return ret;
    }



    private double[] getRadialSplit(double r, double[] sla) {
        return getRadialSplit(r, 0, sla);
    }



    private double[] getRadialSplit(double r, int ansplit, double[] sla) {
        double[] ret = null;
        int nsplit = ansplit;

        double rr = r;
        int nsur = 0;
        while (nsur < sla.length && rr > 2 * sla[nsur]) {
            rr -= sla[nsur];
            nsur += 1;
        }
        int nre = 1;
        if (ansplit > 0) {
            nre = ansplit - nsur;
        } else {
            nre = (int)Math.round(rr / baseDelta);
            if (nre < 1) {
                nre = 1;
            }
        }


        ret = new double[nre + nsur];
        for (int i = 0; i < nre; i++) {
            ret[i] = ((i + 1.)/(nre)) * rr;
        }
        for (int i = 0; i < nsur; i++) {
            ret[nre + i] = ret[nre + i - 1] + sla[nsur - 1 - i];
        }

        assert Math.abs(ret[nre + nsur - 1] - r) < 1.e-6;

        String s = " ";
        for (int i = 0; i < ret.length; i++) {
            s += ret[i] + " ";
        }
        return ret;
    }




    public CurvedVolumeElement getRAElement(int ir, int ia) {
        return radHM.get(ir).get(ia);
    }




    public void planeConnect(CurvedVolumeSlice vg) {
        double[] ras = getRadii(1);
        int[] zas = getNazimuthals();

        double[] rbs = vg.getRadii(0);
        int[] zbs = vg.getNazimuthals();

        double eps = 1.e-6;

        for (int ira = 0; ira < ras.length; ira++) {
            double ra = ras[ira];
            double ra0 = (ira > 0 ? ras[ira-1] : 0);

            for (int irb = 0; irb < rbs.length; irb++) {
                double rb = rbs[irb];
                double rb0 = (irb > 0 ? rbs[irb-1] : 0);

                if (rb < ra0 + eps) {
                    // b elt completely below a elt
                } else if (rb0 > ra - eps) {
                    // b elt comletely above a

                } else {
                    // they overlap
                    //   E.info("olrings " + ira + " " + irb + "     " + ra0 + " " + ra + "    " + rb0 + " " + rb);


                    double ro0 = (ra0 > rb0 ? ra0 : rb0);
                    double ro1 = (ra < rb ? ra : rb);
                    double olarea = Math.PI * (ro0 * ro0  +  ro1 * ro1  +  ro0 * ro1) / 3.;

                    int na = zas[ira];
                    int nb = zbs[irb];

                    if (na == nb) {
                        // E.info("PCexact " + ira + " " + irb + " " + na);
                        double carea = olarea / na;
                        // they match up exactly - simple
                        for (int iz = 0; iz < na; iz++) {
                            getRAElement(ira, iz).coupleTo(vg.getRAElement(irb, iz), carea);

                        }

                    } else {
                        double da = 1. / na;
                        double db = 1. / nb;

                        int izb = 0;

                        for (int iza = 0; iza < na; iza++) {
                            double a0 = iza * da;
                            double a1 = a0 + da;

                            while (izb * db < a1 - eps) {
                                double b0 = izb * db;
                                double b1 = b0 + db;
                                double fc = Math.min(b1, a1) - Math.max(a0, b0);

                                if (fc > eps) {
                                    getRAElement(ira, iza).coupleTo(vg.getRAElement(irb, izb), fc * olarea);
                                }
                                // E.info("PCol " + ira + " " + irb + " " + na + " " + nb + "    " + iza + " " + izb);

                                b0 = b1;
                                b1 += db;
                                izb += 1;
                            }
                            izb -= 1;

                        }

                    }
                }
            }


        }

        // vg is the next slice, startiong at our pb
    }

    public void subPlaneConnect(TreePoint tp, TreePoint tpn, CurvedVolumeSlice vg, double partBranchOffset) {
        planeConnect(vg);
        // MUSTDO - this ignores the partBranchOffset, and conects them as though they were aligned
    }

    public ArrayList<CurvedVolumeElement> getElements() {
        return elements;
    }
}
