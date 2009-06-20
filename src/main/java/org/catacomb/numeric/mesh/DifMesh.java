package org.catacomb.numeric.mesh;

import org.catacomb.interlish.structure.Mesh;
import org.catacomb.numeric.difnet.NetFactory;
import org.catacomb.numeric.difnet.NetStructure;



public class DifMesh implements Mesh {

    MeshPoint[] points;

    int[] indexTable;


    public void setPoints(MeshPoint[] dmpa) {
        points = dmpa;
    }


    public MeshPoint[] getPoints() {
        return points;
    }

    public MeshPoint getPoint(int ipt) {
        return points[ipt];
    }


    public int[] getRemeshMap() {
        if (indexTable == null) {
            makeIndexTable();
        }
        return indexTable;
    }

    /*
    public int getIndexForPoint(int srcidx) {
          // returns index in redescretized array for origina srcidx;
       if (indexTable == null) {
          makeIndexTable();
       }
       int ret = -1;
       if (srcidx >= 0 && srcidx < indexTable.length) {
          ret = indexTable[srcidx];
       } else {
          E.warning("out of range? " + srcidx + " " + indexTable.length);
       }
       return ret;
    }
    */

    private void makeIndexTable() {
        int maxp = -1;
        int np  = points.length;
        int[] bufpa = new int[np];
        for (int i = 0; i < np; i++) {
            int isrc = points[i].getIDIndex();
            if (isrc >= 0 && isrc < np) {
                bufpa[isrc] = i;
                if (isrc > maxp) {
                    maxp = isrc;
                }
            }
        }
        if (maxp < 0) {
            indexTable = new int[0];
        } else {
            indexTable = new int[maxp+1];
            for (int i = 0; i < maxp; i++) {
                indexTable[i] = bufpa[i];
            }
        }
    }





    public void rediscretize(double disqrtr, int nmax) {

        MeshPoint[] mpa = Discretizer.discretize(points, disqrtr, nmax);

        points = mpa;
        indexTable = null;
    }


    public int getSize() {
        return points.length;
    }



    public NetStructure makeNetStructure(NetFactory nf) {

        NetStructure netStructure = MeshNetConverter.meshToNet(points, nf);
        return netStructure;
    }

}
