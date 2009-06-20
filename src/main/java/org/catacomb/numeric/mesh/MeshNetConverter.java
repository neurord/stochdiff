package org.catacomb.numeric.mesh;

import org.catacomb.numeric.difnet.NetFactory;
import org.catacomb.numeric.difnet.NetStructure;
import org.catacomb.numeric.difnet.StructureLink;
import org.catacomb.numeric.difnet.StructureNode;


public class MeshNetConverter {



    public static NetStructure meshToNet(MeshPoint[] pts, NetFactory netFactory) {
        int nl = 0;
        for (int i = 0; i < pts.length; i++) {
            pts[i].setWork(i);
            nl += pts[i].getNeighborCount();
        }
        nl /= 2; // so far, all links counted twice

        StructureNode[] nodes = netFactory.newNodeArray(pts.length);
        StructureLink[] links = netFactory.newLinkArray(nl);


        for (int i = 0; i < pts.length; i++) {

            MeshPoint mp = pts[i];

            StructureNode snode = netFactory.newStructureNode();
            nodes[i] = snode;

            snode.setPosition(mp.getX(), mp.getY(), mp.getZ());
            snode.setRadius(mp.getR());


        }

        nl = 0;
        for (int i = 0; i < pts.length; i++) {
            MeshPoint mp = pts[i];

            int nn = mp.getNeighborCount();
            MeshPoint[] mpn = mp.getNeighbors();

            for (int j = 0; j < nn; j++) {
                if (mpn[j].getWork() > i) {
                    StructureLink slink = netFactory.newStructureLink();
                    slink.setNodeA(nodes[i]);
                    slink.setNodeB(nodes[mpn[j].getWork()]);

                    links[nl++] = slink;
                }
            }
        }

        NetStructure netStructure = netFactory.newNetStructure(nodes, links);

        return netStructure;
    }

}
