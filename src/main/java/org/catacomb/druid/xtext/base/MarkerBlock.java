package org.catacomb.druid.xtext.base;


public class MarkerBlock extends Block {

    ContainerBlock parentCB;

    public MarkerBlock(ContainerBlock p) {
        parentCB = p;
    }

    public ContainerBlock getParent() {
        return parentCB;
    }

}
