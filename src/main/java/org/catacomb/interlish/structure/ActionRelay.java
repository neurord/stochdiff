package org.catacomb.interlish.structure;



public interface ActionRelay {


    void action(String methodName);

    void actionS(String methodName, String sarg);

    void actionB(String methodName, boolean barg);

    void actionI(String methodName, int iarg);

    void actionD(String methodName, double darg);

    void actionO(String methodName, Object oarg);



}
