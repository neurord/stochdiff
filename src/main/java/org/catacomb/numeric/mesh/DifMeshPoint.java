package org.catacomb.numeric.mesh;


public class DifMeshPoint implements MeshPoint {

    double xpos;
    double ypos;
    double zpos;
    double radius;

    int idIndex;
    // this is the index in the original structure for identification
    // purposes - remains the same after remeshing

    int nnbr;
    DifMeshPoint[] nbrs;

    int iwork;

    //   DifMeshPoint[] sna;


    public DifMeshPoint() {
        nbrs = new DifMeshPoint[4];
        idIndex = -1;
    }


    public DifMeshPoint(double x, double y, double z, double r, int idx) {
        this();
        xpos = x;
        ypos = y;
        zpos = z;
        radius = r;

        idIndex = idx;
    }



    public int getNeighborCount() {
        return nnbr;
    }


    public MeshPoint[] getNeighbors() {
        return nbrs;
    }

    /*
      followuing buffers in an array of just the right length;
       if (sna == null || sna.length != nnbr) {
     sna = new MeshPoint[nnbr];
     for (int i = 0; i < nnbr; i++) {
        sna[i] = nbrs[i];
     }
       }
    }
    */


    public void addNeighbor(MeshPoint nnn) {
        // POSERR can give an array bounds errror!!!!!!!!
        nbrs[nnbr] = (DifMeshPoint)nnn;
        nnbr += 1;
    }

    public void replaceNeighbor(MeshPoint oldNode, MeshPoint newNode) {
        for (int i = 0; i < nnbr; i++) {
            if (nbrs[i] == oldNode) {
                nbrs[i] = (DifMeshPoint)newNode;
            }
        }
    }



    public void disconnect() {
        // kill links once node is dead;
        nnbr = 0;
    }


    public void setWork(int i) {
        iwork = i;
    }

    public int getWork() {
        return iwork;
    }


    public double getX() {
        return xpos;
    }

    public double getY() {
        return ypos;
    }

    public double getZ() {
        return zpos;
    }

    public double getR() {
        return radius;
    }

    public int getIDIndex() {
        return idIndex;
    }

    public void setX(double x) {
        xpos = x;
    }

    public void setY(double y) {
        ypos = y;
    }

    public void setZ(double z) {
        zpos = z;
    }

    public void setR(double r) {
        radius = r;
    }



    // constuctors for the particular implementing class;
    public MeshPoint newPoint() {
        return new DifMeshPoint();
    }

    public MeshPoint[] newPointArray(int n) {
        return new DifMeshPoint[n];
    }

}


