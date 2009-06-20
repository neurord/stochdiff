package org.catacomb.interlish.structure;


public interface MeshBuilder {


    void startMesh();

    Object newPoint(double x, double y, double z, double r, int idx, Object peer);

    void connectToPeer(Object mp1, Object peer2);

    Mesh getMesh();

}
