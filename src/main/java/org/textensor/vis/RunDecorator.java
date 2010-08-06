package org.textensor.vis;


public class RunDecorator implements Runnable {

    Icing3DViewer viewer;
    Object object;


    public RunDecorator(Icing3DViewer vwr, Object obj) {
        viewer = vwr;
        object = obj;
    }


    public void run() {
        viewer.reallyRefreshDecoration(object);
    }

}
