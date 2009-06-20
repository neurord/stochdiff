package org.catacomb.numeric.mesh;

import org.catacomb.interlish.structure.Mesh;
import org.catacomb.interlish.structure.MeshBuilder;

import java.util.HashMap;



public class DifMeshBuilder implements MeshBuilder {


    DifMesh dmesh;

    int npoint;
    MeshPoint[] points;

    HashMap<Object, DifMeshPoint> peerHM;




    public void startMesh() {
        peerHM = new HashMap<Object, DifMeshPoint>();
        points = new DifMeshPoint[100];
        npoint = 0;
    }



    public Object newPoint(double x, double y, double z, double r, int idx,
                           Object peer) {
        DifMeshPoint dmp = new DifMeshPoint(x, y, z, r, idx);

        peerHM.put(peer, dmp);

        addPoint(dmp);

        return dmp;
    }



    public void addPoint(DifMeshPoint pt) {

        if (npoint >= points.length) {
            MeshPoint[] dmpa = new MeshPoint[(3 * npoint) / 2];
            for (int i  = 0; i < npoint; i++) {
                dmpa[i] = points[i];
            }
            points = dmpa;
        }

        points[npoint++] = pt;
    }



    private DifMeshPoint getPoint(Object obj) {
        return peerHM.get(obj);
    }




    public void connectToPeer(Object mp1, Object o2) {
        DifMeshPoint dmpa = (DifMeshPoint)mp1;
        DifMeshPoint dmpb = getPoint(o2);

        dmpa.addNeighbor(dmpb);
        dmpb.addNeighbor(dmpa);
    }





    public Mesh getMesh() {
        DifMesh dm= new DifMesh();

        trimPointArray();

        dm.setPoints(points);

        return dm;
    }




    public void trimPointArray() {
        if (points.length != npoint) {

            MeshPoint[] dmpa = new MeshPoint[npoint];
            for (int i = 0; i < npoint; i++) {
                dmpa[i] = points[i];
            }

            points = dmpa;
        }
    }

}
