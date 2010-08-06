package org.textensor.vis;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.HashSet;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Light;
import javax.media.j3d.Locale;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.PickConeRay;
import javax.media.j3d.PickCylinderRay;
import javax.media.j3d.PickInfo;
import javax.media.j3d.PickRay;
import javax.media.j3d.PickShape;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.textensor.report.E;


import com.sun.j3d.utils.geometry.Sphere;



public class SceneGraphViewer implements ActionListener, MouseListener, MouseMotionListener {

    JPanel panel;

    Canvas3D canvas;

    VirtualUniverse universe;
    Locale locale;
    View view;

    int xdown;
    int ydown;
    boolean dragging = false;

    public final static int PAN = 0;
    public final static int ZOOM = 1;
    public final static int ROLL = 2;
    int mode = PAN;


    public final static int NONE = 0;
    public final static int LEFT = 1;
    public final static int RIGHT = 2;
    int button = NONE;


    Transform3D rootTransform;
    TransformGroup rootTransformGroup;

    TransformGroup decTransformGroup;

    BranchGroup baseGroup;


    HashMap<String, SceneItem> sceneItemHM = new HashMap<String, SceneItem>();


    float scale = 1.f;
    Transform3D downTransform;
    Point3d wcdown;

    Transform3D fcTrans = new Transform3D();
    Transform3D tcTrans = new Transform3D();

    Transform3D fwTrans = new Transform3D();
    Transform3D twTrans = new Transform3D();


    Transform3D fwrcTrans = new Transform3D();
    Transform3D twrcTrans = new Transform3D();

    float downscale = 0.f;


    float brightness = 0.7f;

    BranchGroup lights;

    DirectionalLight directionalLight;
    AmbientLight ambientLight;


    public SceneGraphViewer() {

        GraphicsConfiguration config = getPreferredConfiguration();
        canvas = new Canvas3D(config);
        canvas.setPreferredSize(new Dimension(400, 400));
        canvas.setFocusable(true);


        panel = new JPanel();

        panel.setLayout(new BorderLayout());

        JPanel jp = new JPanel();
        jp.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));

        JRadioButton bpan = new JRadioButton("pan", true);
        bpan.setActionCommand("pan");
        bpan.addActionListener(this);

        JRadioButton bzoom = new JRadioButton("zoom");
        bzoom.setActionCommand("zoom");
        bzoom.addActionListener(this);

        JRadioButton broll = new JRadioButton("roll");
        broll.setActionCommand("roll");
        broll.addActionListener(this);

        ButtonGroup g = new ButtonGroup();
        g.add(bpan);
        g.add(bzoom);
        g.add(broll);

        jp.add(bpan);
        jp.add(bzoom);
        jp.add(broll);


        panel.add(jp, BorderLayout.NORTH);

        panel.add(canvas, BorderLayout.CENTER);



        universe = new VirtualUniverse();
        locale = new Locale(universe);


        ViewPlatform platform = new ViewPlatform();
        TransformGroup tg = new TransformGroup();
        Transform3D wk = new Transform3D();
        wk.setTranslation(new Vector3d(0., 0., 100.));
        tg.setTransform(wk);
        tg.addChild(platform);
        BranchGroup platformBG = new BranchGroup();
        platformBG.addChild(tg);

        locale.addBranchGraph(platformBG);


        view = new View();
        view.setPhysicalBody(new PhysicalBody());
        view.setPhysicalEnvironment(new PhysicalEnvironment());

        //   view.setSceneAntialiasingEnable(true);
        view.setProjectionPolicy(View.PARALLEL_PROJECTION);
        view.setFrontClipDistance(1.);
        view.setBackClipDistance(200.);
        //	view.setFieldOfView(5. * Math.PI / 180.);
        view.addCanvas3D(canvas);
        view.attachViewPlatform(platform);


        lights = createLightGraph(brightness);
        //	lights.setCapability(BranchGroup.ALLOW_DETACH);
        //	lights.compile();
        locale.addBranchGraph(lights);

        BranchGroup scene = createDummySceneGraph();

        setSceneGraph(scene);

        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
    }



    public void deltaLights(double d) {
        double br = brightness;
        br += d;
        if (br > 1.) {
            br = 1.;
        }
        if (br < 0.) {
            br = 0.;
        }
        setBrightness(br);
    }



    public void setBrightness(double br) {
        if (Math.abs(br - brightness) > 0.01) {
            applyBrightness(br);
        }
    }

    private void applyBrightness(double br) {
        brightness = (float)br;
        Color3f c3 = new Color3f(brightness, brightness, brightness);
        ambientLight.setColor(c3);
        directionalLight.setColor(c3);
    }



    public JPanel getPanel() {
        return panel;
    }



    public void setSceneGraph(BranchGroup bg) {
        if (baseGroup != null) {
            locale.removeBranchGraph(baseGroup);
        }

        if (rootTransform == null) {
            rootTransform= new Transform3D();
            rootTransform.setTranslation(new Vector3d(0., 0., 0.));
            rootTransform.setScale(5.f);
        }

        rootTransformGroup = new TransformGroup();
        rootTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        rootTransformGroup.setTransform(rootTransform);
        rootTransformGroup.addChild(bg);

        baseGroup = new BranchGroup();
        baseGroup.addChild(rootTransformGroup);
        baseGroup.setCapability(BranchGroup.ALLOW_DETACH);
        baseGroup.compile();
        locale.addBranchGraph(baseGroup);


        if (decTransformGroup == null) {
            decTransformGroup = new TransformGroup();
            decTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            decTransformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
            decTransformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
            decTransformGroup.setTransform(rootTransform);
            BranchGroup decGroup = new BranchGroup();
            decGroup.addChild(decTransformGroup);
            locale.addBranchGraph(decGroup);
        }
    }


    private BranchGroup createDummySceneGraph() {
        BranchGroup ret = new BranchGroup();

        for (int i = 0; i < 50; i++) {
            TransformGroup trans = new TransformGroup();
            Transform3D pos = new Transform3D();
            pos.setTranslation(new Vector3f((float)(-1 + 2 * Math.random()), (float)(-1 + 2  * Math.random()),
                                            (float)(-0.1 + 0.2 * Math.random())));
            trans.setTransform(pos);
            ret.addChild(trans);

            Sphere sp = new Sphere((float)(0.01 + 0.1 * Math.random()), Sphere.GENERATE_NORMALS, 50);
            trans.addChild(sp);
        }

        return ret;
    }






    private BranchGroup createLightGraph(double br) {
        float fb = (float)br;
        BranchGroup group = new BranchGroup();
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

        // directed;
        Color3f light1Color = new Color3f(fb, fb, fb);
        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -2.0f);
        directionalLight = new DirectionalLight(light1Color, light1Direction);
        directionalLight.setInfluencingBounds(bounds);
        directionalLight.setCapability(Light.ALLOW_COLOR_WRITE);
        group.addChild(directionalLight);

        /*
        Color3f light2Color = new Color3f(.3f, .3f, 0.f);
        Vector3f light2Direction = new Vector3f(-2.0f, -7.0f, -5.0f);
        DirectionalLight light2 = new DirectionalLight(light2Color, light2Direction);
        light2.setInfluencingBounds(bounds);
        group.addChild(light2);
        */

        // ambient;
        Color3f ambientColor = new Color3f(fb, fb, fb);
        ambientLight = new AmbientLight(ambientColor);
        ambientLight.setCapability(Light.ALLOW_COLOR_WRITE);
        ambientLight.setInfluencingBounds(bounds);
        group.addChild(ambientLight);

        return group;
    }



    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("pan")) {
            mode = PAN;

        } else if (s.equals("zoom")) {
            mode = ZOOM;

        } else if (s.equals("roll")) {
            mode = ROLL;

        } else {
            E.error("unhandled " + s);
        }
    }



    public void mouseClicked(MouseEvent e) {
    }



    public void mousePressed(MouseEvent e) {
        dragging = false;
        downscale = scale;
        xdown = e.getX();
        ydown = e.getY();


        downTransform = new Transform3D(rootTransform);
        Transform3D motion = new Transform3D();
        canvas.getImagePlateToVworld(motion);

        Point3d mouse_pos = new Point3d();
        wcdown = new Point3d();
        canvas.getPixelLocationInImagePlate(xdown, ydown, mouse_pos);
        motion.transform(mouse_pos, wcdown);

        wcdown.z = 0;

        Transform3D t = new Transform3D(rootTransform);
        t.invert();
        Point3d cc = new Point3d();
        t.transform(wcdown, cc);
        Vector3d vc = new Vector3d(cc);

        tcTrans = new Transform3D();
        tcTrans.setTranslation(vc);

        fcTrans = new Transform3D();
        vc.negate();
        fcTrans.setTranslation(vc);

        Vector3d vw = new Vector3d(wcdown);
        twTrans = new Transform3D();
        twTrans.setTranslation(vw);

        fwTrans = new Transform3D();
        vw.negate();
        fwTrans.setTranslation(vw);





        Transform3D t3 = getPickTransform(e.getX(), e.getY());
        if (t3 != null) {
            Point3d p3 = new Point3d();
            t3.transform(p3);
            Vector3d vp = new Vector3d(p3);
            twrcTrans = new Transform3D();
            twrcTrans.setTranslation(vp);

            fwrcTrans = new Transform3D();
            vp.negate();
            fwrcTrans.setTranslation(vp);


        } else {
            // best guess for rotation center is point in z=0 plane under mouse
            fwrcTrans = fwTrans;
            twrcTrans = twTrans;
        }


        button = NONE;
        int m = e.getModifiers();
        button = LEFT;

        if ((m & InputEvent.BUTTON3_MASK) != 0) {
            button = RIGHT;
        } else {
            int[] masks = {InputEvent.CTRL_MASK, InputEvent.META_MASK, InputEvent.SHIFT_MASK,
                           InputEvent.BUTTON2_MASK
                          };
            for (int i = 0; i < masks.length; i++) {
                if ((m & masks[i]) != 0) {
                    button = RIGHT;
                    break;
                }
            }
        }
    }



    public void mouseReleased(MouseEvent e) {

        if (dragging) {

        } else if (mode == ZOOM) {
            // no click actions for zoom;

        } else {
            if (button == LEFT) {
                zoomBy(1. / 0.7);
            } else if (button == RIGHT) {
                zoomBy(0.7);

            }
        }
    }


    private void zoomBy(double d) {
        zoomTo(scale * d);
    }

    private void zoomTo(double d) {
        scale = (float)d;

        double fscale = d / downscale;
        Transform3D tscale = new Transform3D();
        tscale.setScale(fscale);
        Transform3D tnew = new Transform3D();
        /*
        		tnew.mul(downTransform);
        		tnew.mul(tcTrans);
        		tnew.mul(tscale);
        		tnew.mul(fcTrans);
        */

        tnew.mul(twTrans);
        tnew.mul(tscale);
        tnew.mul(fwTrans);
        tnew.mul(downTransform);

        setTransform(tnew);
    }

    private void setTransform(Transform3D t3d) {
        rootTransform = t3d;
        rootTransformGroup.setTransform(rootTransform);
        decTransformGroup.setTransform(rootTransform);
    }



    @SuppressWarnings("unused")
    public void intPan(int dx, int dy, Point3d cpos) {


        Transform3D ttrans = new Transform3D();
        Vector3d v = new Vector3d(cpos.x - wcdown.x, cpos.y - wcdown.y, 0.);
        // v.scale(1. / scale);
        ttrans.setTranslation(v);

        Transform3D tnew = new Transform3D();

        tnew.mul(ttrans);
        tnew.mul(downTransform);
        setTransform(tnew);
    }



    private void intRoll(int dx, int dy) {

        double wdx = dx; // cpos.x - wcdown.x;
        double wdy = dy; // cpos.y - wcdown.y;

        Transform3D trot = new Transform3D();
        double theta = 0.005 * Math.sqrt(dx * dx + dy * dy);
        AxisAngle4d aa4d = new AxisAngle4d(wdy, wdx, 0., theta);
        trot.setRotation(aa4d);


        Transform3D tnew = new Transform3D();

        tnew.mul(twrcTrans);
        tnew.mul(trot);
        tnew.mul(fwrcTrans);
        tnew.mul(downTransform);

        setTransform(tnew);
    }






    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (!dragging && (x-xdown)*(x-xdown) + (y-ydown)*(y-ydown) > 10) {
            dragging = true;
        }
        if (dragging) {

            Point3d cpos = new Point3d();
            canvas.getPixelLocationInImagePlate(x, y, cpos);
            Transform3D motion = new Transform3D();
            canvas.getImagePlateToVworld(motion);
            motion.transform(cpos);

            if (mode == PAN) {
                intPan(x - xdown,  y - ydown, cpos);

            } else if (mode == ZOOM) {
                double f = Math.exp(0.01 * (y - ydown));
                zoomTo(f * downscale);

            } else {
                if (button == LEFT) {
                    intRoll(x - xdown, y - ydown);
                } else {
                    intPan(x - xdown,  y - ydown, cpos);
                }
            }
        }


    }





    private Transform3D getPickTransform(int xpos, int ypos) {
        float tolerance = 0.1f;

        Transform3D motion = new Transform3D();
        Point3d eyePosn = new Point3d();
        Point3d mousePosn = new Point3d();
        Vector3d mouseVec = new Vector3d();
        boolean isParallel = false;
        double spreadAngle = 0.0;

        canvas.getCenterEyeInImagePlate(eyePosn);
        canvas.getPixelLocationInImagePlate(xpos,ypos,mousePosn);

        if ((canvas.getView() != null) &&
                (canvas.getView().getProjectionPolicy() == View.PARALLEL_PROJECTION)) {
            // just use a ray through the mouse position
            eyePosn.x = mousePosn.x;
            eyePosn.y = mousePosn.y;
            isParallel = true;
        }

        // Calculate radius for PickCylinderRay and spread angle for PickConeRay
        Vector3d eyeToCanvas = new Vector3d();
        eyeToCanvas.sub(mousePosn, eyePosn);
        double distanceEyeToCanvas = eyeToCanvas.length();

        Point3d deltaImgPlate = new Point3d();
        canvas.getPixelLocationInImagePlate(xpos+1, ypos, deltaImgPlate);

        Vector3d ptToDelta = new Vector3d();
        ptToDelta.sub(mousePosn, deltaImgPlate);
        double distancePtToDelta = ptToDelta.length();
        distancePtToDelta *= tolerance;

        canvas.getImagePlateToVworld(motion);

        motion.transform(eyePosn);
        motion.transform(mousePosn);
        mouseVec.sub(mousePosn, eyePosn);
        mouseVec.normalize();


        PickShape pickShape = null;

        if (tolerance == 0.0) {
            pickShape = new PickRay(eyePosn, mouseVec);

        } else if (isParallel) {
            pickShape = new PickCylinderRay(eyePosn, mouseVec, distancePtToDelta);

        } else {
            // Perspective projection, use a PickConeRay
            // Calculate spread angle
            spreadAngle = Math.atan(distancePtToDelta/distanceEyeToCanvas);
            pickShape = new PickConeRay(eyePosn, mouseVec, spreadAngle);
        }


        PickInfo pickInfo = baseGroup.pickClosest(PickInfo.PICK_BOUNDS, PickInfo.LOCAL_TO_VWORLD, pickShape);

        Transform3D ret = null;
        if (pickInfo != null) {
            ret = pickInfo.getLocalToVWorld();
        }
        return ret;
    }




    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public static GraphicsConfiguration getPreferredConfiguration() {
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        // could set stereo stuff here (see SimpleUniverse)

        return GraphicsEnvironment.getLocalGraphicsEnvironment().
               getDefaultScreenDevice().getBestConfiguration(template);
    }




    public void setDecoration(String id, BranchGroup root, boolean b) {
        root.setCapability(BranchGroup.ALLOW_DETACH);
        root.compile();
        if (sceneItemHM.containsKey(id)) {
            setDecorationVisibility(id, false);
            sceneItemHM.remove(sceneItemHM.get(id));
        }
        SceneItem sit = new SceneItem(id, root);
        sceneItemHM.put(id, sit);
        setDecorationVisibility(id, b);
    }


    public void removeUnlistedDecoration(HashSet<String> keepHS) {
        for (String s : sceneItemHM.keySet()) {
            if (keepHS != null && keepHS.contains(s)) {
                // OK;
            } else {
                setDecorationVisibility(s, false);
                sceneItemHM.remove(sceneItemHM.get(s));
            }
        }
    }


    public void removeAllDecoration() {
        removeUnlistedDecoration(null);
    }


    public synchronized void setDecorationVisibility(String s, boolean b) {
        SceneItem sit = sceneItemHM.get(s);
        if (sit.showing) {
            if (b) {
                // OK;
            } else {
                decTransformGroup.removeChild(sit.getBranchGroup());
                sit.showing = false;
            }

        } else  {
            if (b) {
                sit.showing = true;
                decTransformGroup.addChild(sit.getBranchGroup());
            } else {
                // OK;
            }
        }
    }



    public void setAA(boolean b) {
        view.setSceneAntialiasingEnable(b);
    }



    public void setLightsPercent(int p) {
        setBrightness(p / 100.);

    }



    public void setFourMatrix(double[] fmo) {
        Matrix4d m4d = new Matrix4d();
        m4d.set(fmo);
        Transform3D t3d = new Transform3D(m4d);
        setTransform(t3d);
    }



    public double[] getFourMatrix() {
        double[] mtx = new double[16];
        rootTransform.get(mtx);
        return mtx;
    }




}
