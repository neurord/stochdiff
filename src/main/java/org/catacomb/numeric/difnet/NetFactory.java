package org.catacomb.numeric.difnet;


public interface NetFactory {


    NetStructure newNetStructure(StructureNode[] nodes, StructureLink[] links);

    StructureNode[] newNodeArray(int n);

    StructureLink[] newLinkArray(int n);

    StructureNode newStructureNode();

    StructureLink newStructureLink();


}
