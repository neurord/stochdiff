package org.textensor.vis;


public class RunBuilder implements Runnable {

    Icing3DViewer viewer;
    Object object;


    public RunBuilder(Icing3DViewer vwr, Object obj) {
        viewer = vwr;
        object = obj;

    }


    public void run() {
        viewer.reallyBuildVewable(object);
    }

}
