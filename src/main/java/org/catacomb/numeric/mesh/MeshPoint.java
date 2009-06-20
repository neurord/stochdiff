
package org.catacomb.numeric.mesh;

/*
  A NodeNet is one where the entire net is specified by nodes, which have
  get-neighbors method;

*/


public interface MeshPoint {

    int getNeighborCount();

    MeshPoint[] getNeighbors();


    int getIDIndex();

    void addNeighbor(MeshPoint nnn);

    void replaceNeighbor(MeshPoint oldNode, MeshPoint newNode);

    void disconnect();  // kill links once node is dead;

    void setWork(int i);

    int getWork();


    double getX();
    double getY();
    double getZ();
    double getR();


    void setX(double x);
    void setY(double y);
    void setZ(double z);
    void setR(double r);



    // constuctors for the particular implementing class;
    MeshPoint newPoint();

    MeshPoint[] newPointArray(int n);


}
