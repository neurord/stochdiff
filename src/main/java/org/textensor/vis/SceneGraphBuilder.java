package org.textensor.vis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;


public class SceneGraphBuilder {


    BranchGroup baseGroup = null;

    HashMap<Color, Appearance> appHM = new HashMap<Color, Appearance>();

    Appearance defaultAppearance;

    Appearance[] randomAppearances;

    ArrayList<Shape3D> shapes;


    public SceneGraphBuilder() {
        defaultAppearance = makeDefaultAppearance();
        randomAppearances = makeRandomAppearances(20);
    }


    private Appearance makeDefaultAppearance() {
        Color3f aColor  = new Color3f(0.3f, 0.3f, 0.3f); // ambient
        Color3f eColor  = new Color3f(0.0f, 0.0f, 0.0f); // emmissive
        Color3f dColor  = new Color3f(0.6f, 0.6f, 0.6f); // diffuse
        Color3f sColor  = new Color3f(0.8f, 0.8f, 0.8f); // specular

        Material m = new Material(aColor, eColor, dColor, sColor, 70.0f);
        // specularity is on a range form 0 to 128
        Appearance a = new Appearance();
        m.setLightingEnable(true);
        a.setMaterial(m);
        return a;
    }

    private Appearance[] makeRandomAppearances(int n) {
        Random random = new Random();
        ArrayList<Appearance> ala = new ArrayList<Appearance>();
        for (int i = 0; i < n; i++) {
            Color3f aColor  = randomColor(random, 0.3); // ambient
            Color3f eColor  = new Color3f(0.0f, 0.0f, 0.0f);  // emmissive
            Color3f dColor  =  rescale(aColor, 0.6); //randomColor(random, 0.6);// diffuse
            Color3f sColor  = rescale(aColor, 0.8); // randomColor(random, 0.8); // specular

            Material m = new Material(aColor, eColor, dColor, sColor, 70.0f);
            // specularity is on a range form 0 to 128
            Appearance a = new Appearance();
            m.setLightingEnable(true);
            a.setMaterial(m);
            /*
            TransparencyAttributes ta = new TransparencyAttributes();
            ta.setTransparencyMode (TransparencyAttributes.SCREEN_DOOR);
            ta.setTransparency(0.1f);
            a.setTransparencyAttributes(ta);
             */
            ala.add(a);
        }
        return ala.toArray(new Appearance[0]);
    }


    private Color3f rescale(Color3f c, double d) {
        Color col = c.get();
        double fr = col.getRed() / 256.;
        double fg = col.getGreen() / 256.;
        double fb = col.getBlue() / 256.;
        double f = (fr + fg + fb) / 3;
        Color3f ret = new Color3f(flimit(d * fr / f), flimit(d * fg / f), flimit(d * fb / f));
        return ret;
    }

    private float flimit(double x) {
        double y = x;
        if (y < 0.) {
            y = 0.;
        }
        if (y > 1.) {
            y = 1;
        }
        return (float)y;
    }

    private Color3f randomColor(Random r, double d) {
        double red = rcol(r, d);
        double green = rcol(r, 1.5 * d);
        double blue = rcol(r, d);

        Color3f ret  = new Color3f((float)red, (float)green, (float)blue);
        return ret;
    }


    private double rcol(Random r, double d) {
        double v = d + 0.15 * r.nextGaussian();
        if (v > 1.0) {
            v = 1.0;
        }
        if (v < 0.0) {
            v = 0.0;
        }
        return v;
    }



    private Appearance getAppearance(Color c) {
        Appearance ret = null;
        if (appHM.containsKey(c)) {
            ret = appHM.get(c);

        } else {
            Color3f aColor  = new Color3f(c.darker());
            Color3f eColor  = new Color3f(0.0f, 0.0f, 0.0f); // emmissive
            Color3f dColor  = new Color3f(c);
            Color3f sColor  = new Color3f(c.brighter());

            Material m = new Material(aColor, eColor, dColor, sColor, 70.0f);
            // specularity is on a range form 0 to 128
            ret = new Appearance();
            m.setLightingEnable(true);
            ret.setMaterial(m);
            appHM.put(c, ret);
        }

        return ret;
    }




    public void buildTree(IcingPoint[] points, int res, double fac) {
        // Timer t = new Timer();
        buildFlatTree(points, res, fac);
        // t.show("building base tree");
    }

    private void buildFlatTree(IcingPoint[] points, int res, double fac) {
        baseGroup = new BranchGroup();

        for (IcingPoint p : points) {
            double x = p.getX();
            double y = p.getY();
            double z = p.getZ();
            double r = p.getR();

            IcingPoint ppar = p.getParent();

            if (ppar == null) {
                // may or may not need a ball for it
                if (p.isBall()) {
                    TransformGroup ctrans = new TransformGroup();
                    Transform3D cpos = new Transform3D();
                    cpos.setTranslation(new Vector3d(fac * x, fac * y, fac * z));
                    ctrans.setTransform(cpos);

                    Shape3D s = new Shape3D();
                    if (p.isColored3d()) {
                        s.setAppearance(getAppearance(p.getColor()));
                    } else {
                        s.setAppearance(defaultAppearance);
                    }
                    TriangleStripArray tsa = mkSphereTriangles(fac * r, 5);
                    s.setGeometry(tsa);

                    ctrans.addChild(s);

                    baseGroup.addChild(ctrans);

                }


            } else {
                double dx = ppar.getX() - x;
                double dy = ppar.getY() - y;
                double dz = ppar.getZ() - z;
                double lxy = Math.sqrt(dx * dx + dy * dy);

                double d = Math.sqrt(dx*dx + dy*dy + dz*dz);

                double ex = Math.atan2(dz, lxy);
                double ey = 0.;
                double ez = -Math.atan2(dx, dy);
                Vector3d veuler = new Vector3d(ex, ey, ez);

                TransformGroup ctrans = new TransformGroup();
                Transform3D cpos = new Transform3D();
                cpos.setEuler(veuler);
                cpos.setTranslation(new Vector3d(fac * x, fac * y, fac * z));
                ctrans.setTransform(cpos);

                Shape3D s = new Shape3D();
                if (p.isColored3d()) {
                    s.setAppearance(getAppearance(p.getColor()));
                } else {
                    s.setAppearance(defaultAppearance);
                }


                double pr = ppar.getR();
                if (p.isMinor() || p.uniform()) {
                    pr = p.getR();
                }



                if (res == Visualizer.LOW) {
                    TriangleStripArray tsa = mkCarrotoidTriangles(fac * r, fac * pr, fac * d, 8, 1, 1);
                    s.setGeometry(tsa);

                } else if (res == Visualizer.MEDIUM) {
                    TriangleStripArray tsa = mkCarrotoidTriangles(fac * r, fac * pr, fac * d, 16,
                                             (p.ball ? 3 : 1), (ppar.ball ? 3 : 1));
                    s.setGeometry(tsa);

                } else if (res == Visualizer.HIGH) {
                    TriangleStripArray tsa = mkCarrotoidTriangles(fac * r, fac * pr, fac * d, 22,
                                             (p.ball ? 5 : 1), (ppar.ball ? 5 : 1));
                    s.setGeometry(tsa);
                }


                ctrans.addChild(s);

                baseGroup.addChild(ctrans);
            }
        }


    }




    private TriangleStripArray mkCarrotoidTriangles(double ra, double rb, double d,
            int nside, int ncapa, int ncapb) {

        int nstrip = 1 + ncapa + ncapb;
        int nvert = 2 * nside * nstrip;

        int[] svc = new int[nstrip];
        for (int i = 0; i < nstrip; i++) {
            svc[i] = 2 * nside;
        }

        float[] datv = new float[3 * nvert];
        float[] datn = new float[3 * nvert];


        double dtheta = 2. * Math.PI / (nside-1);
        double[][] csas = new double[nside][2];
        double[][] csbs = new double[nside][2];
        for (int i = 0; i <  nside; i++) {
            double tha = i * dtheta;
            double thb = (i + 0.5) * dtheta;
            csas[i][0] = Math.cos(tha);
            csas[i][1] = Math.sin(tha);

            csbs[i][0] = Math.cos(thb);
            csbs[i][1] = Math.sin(thb);
        }

        double dr = ra - rb;
        double znorm = dr / Math.sqrt(dr*dr + d*d);
        double zr = Math.sqrt(1. - znorm*znorm);
        vnStrip(datv, datn, 0, nside, ra, rb, 0., d, znorm, zr, znorm, zr,  csas, csbs);

        int koff = 0;
        koff += 6 * nside;

        double frad = 1.;
        if (ncapa == 1) {
            frad = 0.;
        }

        for (int ic = 0; ic < ncapa; ic++) {
            double[][] incs = (ic % 2 == 0 ? csbs : csas);
            double[][] outcs = (ic % 2 == 0 ? csas : csbs);

            double t0 = ic * (0.5 * Math.PI / (ncapa + 0.1));
            double t1 = (ic + 1) * (0.5 * Math.PI / (ncapa + 0.1));
            double s0 = Math.sin(t0);
            double c0 = Math.cos(t0);
            double s1 = Math.sin(t1);
            double c1 = Math.cos(t1);
            vnStrip(datv, datn, koff, nside, c1 * ra, c0 * ra, -frad * s1 * ra, -frad * s0 * ra, -s1, c1, -s0, c0, incs, outcs);
            koff += 6 * nside;
        }

        frad = 1.;
        if (ncapb == 1) {
            frad = 0.;
        }
        for (int ic = 0; ic < ncapb; ic++) {
            double[][] incs = (ic % 2 == 0 ? csbs : csas);
            double[][] outcs = (ic % 2 == 0 ? csas : csbs);
            double t0 = ic * (0.5 * Math.PI / (ncapb + 0.1));
            double t1 = (ic + 1) * (0.5 * Math.PI / (ncapb + 0.1));
            double s0 = Math.sin(t0);
            double c0 = Math.cos(t0);
            double s1 = Math.sin(t1);
            double c1 = Math.cos(t1);
            vnStrip(datv, datn, koff, nside, c0 * rb, c1 * rb, d + frad * s0 * rb, d + frad * s1 * rb, s0, c0, s1, c1, incs, outcs);
            koff += 6 * nside;
        }

        TriangleStripArray ret = new TriangleStripArray(nvert,
                GeometryArray.COORDINATES | GeometryArray.NORMALS, svc);
        ret.setCoordinates(0, datv);
        ret.setNormals(0, datn);
        return ret;
    }






    private TriangleStripArray mkSphereTriangles(double ra, int ncap) {
        int nside = 15;

        int nstrip = 2 * ncap;
        int nvert = 2 * nside * nstrip;

        int[] svc = new int[nstrip];
        for (int i = 0; i < nstrip; i++) {
            svc[i] = 2 * nside;
        }

        float[] datv = new float[3 * nvert];
        float[] datn = new float[3 * nvert];


        double dtheta = 2. * Math.PI / (nside-1);
        double[][] csas = new double[nside][2];
        double[][] csbs = new double[nside][2];
        for (int i = 0; i <  nside; i++) {
            double tha = i * dtheta;
            double thb = (i + 0.5) * dtheta;
            csas[i][0] = Math.cos(tha);
            csas[i][1] = Math.sin(tha);

            csbs[i][0] = Math.cos(thb);
            csbs[i][1] = Math.sin(thb);
        }

        int koff = 0;

        for (int ic = 0; ic < ncap; ic++) {
            double[][] incs = (ic % 2 == 0 ? csbs : csas);
            double[][] outcs = (ic % 2 == 0 ? csas : csbs);

            double t0 = ic * (0.5 * Math.PI / (ncap + 0.1));
            double t1 = (ic + 1) * (0.5 * Math.PI / (ncap + 0.1));
            double s0 = Math.sin(t0);
            double c0 = Math.cos(t0);
            double s1 = Math.sin(t1);
            double c1 = Math.cos(t1);
            vnStrip(datv, datn, koff, nside, c1 * ra, c0 * ra, -1 * s1 * ra, -1 * s0 * ra, -s1, c1, -s0, c0, incs, outcs);
            koff += 6 * nside;
        }


        for (int ic = 0; ic < ncap; ic++) {
            double[][] incs = (ic % 2 == 0 ? csbs : csas);
            double[][] outcs = (ic % 2 == 0 ? csas : csbs);
            double t0 = ic * (0.5 * Math.PI / (ncap + 0.1));
            double t1 = (ic + 1) * (0.5 * Math.PI / (ncap + 0.1));
            double s0 = Math.sin(t0);
            double c0 = Math.cos(t0);
            double s1 = Math.sin(t1);
            double c1 = Math.cos(t1);
            vnStrip(datv, datn, koff, nside, c0 * ra, c1 * ra,  s0 * ra,  s1 * ra, s0, c0, s1, c1, incs, outcs);
            koff += 6 * nside;
        }

        TriangleStripArray ret = new TriangleStripArray(nvert,
                GeometryArray.COORDINATES | GeometryArray.NORMALS, svc);
        ret.setCoordinates(0, datv);
        ret.setNormals(0, datn);
        return ret;
    }




    private void vnStrip(float[] datv, float[] datn, int koff, int nside, double ra, double rb, double da, double db, double s0, double c0, double s1, double c1, double[][] incs, double[][] outcs) {

        for (int i = 0; i < nside; i++) {
            int k = koff + 6 * i;
            datv[k] = (float)(ra * incs[i][0]);
            datv[k+1] = (float)da;
            datv[k+2] = (float)(ra * incs[i][1]);
            datv[k+3] = (float)(rb * outcs[i][0]);
            datv[k+4] = (float)db;
            datv[k+5] = (float)(rb * outcs[i][1]);

            datn[k] = (float)(c0 * incs[i][0]);
            datn[k+1] = (float)s0;
            datn[k+2] = (float)(c0 * incs[i][1]);
            datn[k+3] = (float)(c1 * outcs[i][0]);
            datn[k+4] = (float)s1;
            datn[k+5] = (float)(c1 * outcs[i][1]);
        }
    }


    public  BranchGroup getSceneGraph() {
        return baseGroup;
    }


    public void loadElements(ArrayList<VolElt> elements) {
        baseGroup = new BranchGroup();

        shapes = new ArrayList<Shape3D>();

        int iel = 0;
        for (VolElt ve : elements) {

            ve.centroidize();

            TransformGroup ctrans = new TransformGroup();
            Transform3D cpos = new Transform3D();
            cpos.setTranslation(new Vector3d(ve.getCX(), ve.getCY(), ve.getCZ()));
            ctrans.setTransform(cpos);

            Shape3D s = new Shape3D();
            shapes.add(s);

            s.setAppearance(defaultAppearance);

            int ira = (int)(Math.random() * (randomAppearances.length - 0.01));
            Appearance app = randomAppearances[ira];
            Appearance apc = (Appearance)(app.cloneNodeComponent(true));
            apc.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
            s.setAppearance(apc);


            TriangleStripArray tsa = new TriangleStripArray(ve.getNvert(),
                    GeometryArray.COORDINATES | GeometryArray.NORMALS, ve.getLens());
            tsa.setCoordinates(0, ve.getVerts());
            tsa.setNormals(0, ve.getNorms());


            s.setGeometry(tsa);

            ctrans.addChild(s);

            baseGroup.addChild(ctrans);

            iel += 1;

        }
    }


    public ArrayList<Shape3D> getShapes() {
        return shapes;
    }

}