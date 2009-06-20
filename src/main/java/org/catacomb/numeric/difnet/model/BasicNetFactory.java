package org.catacomb.numeric.difnet.model;

import org.catacomb.numeric.difnet.NetFactory;
import org.catacomb.numeric.difnet.NetStructure;
import org.catacomb.numeric.difnet.StructureLink;
import org.catacomb.numeric.difnet.StructureNode;



public class BasicNetFactory implements NetFactory {



    public NetStructure newNetStructure(StructureNode[] nodes, StructureLink[] links) {

        BasicNetStructure bns = new BasicNetStructure();

        bns.setNodes((BasicStructureNode[])nodes);
        bns.setLinks((BasicStructureLink[])links);

        return bns;
    }


    public StructureNode[] newNodeArray(int n) {
        return new BasicStructureNode[n];
    }


    public StructureLink[] newLinkArray(int n) {
        return new BasicStructureLink[n];
    }


    public StructureNode newStructureNode() {
        return new BasicStructureNode();
    }

    public StructureLink newStructureLink() {
        return new BasicStructureLink();
    }


}
