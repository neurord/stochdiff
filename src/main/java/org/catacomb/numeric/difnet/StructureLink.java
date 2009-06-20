package org.catacomb.numeric.difnet;


/** properties of a link in a NetStructure.
 *
 *
 *
 */


public interface StructureLink {

    void setNodeA(StructureNode sn);

    StructureNode getNodeA();

    void setNodeB(StructureNode sn);

    StructureNode getNodeB();

}
