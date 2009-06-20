package org.catacomb.druid.xtext.base;


public class Block extends DMItem {

    public String idInParent;

    Block nextBlock;
    Block previousBlock;



    public Block() {
        idInParent = "none";
    }


    public void setNext(Block b) {
        nextBlock = b;
        nextBlock.setPrevious(this);
    }

    public boolean hasNext() {
        return (nextBlock != null);
    }

    public Block next() {
        return nextBlock;
    }


    private void setPrevious(Block b) {
        previousBlock = b;
    }

    public boolean hasPrevious() {
        return (previousBlock != null);
    }

    public Block previous() {
        return previousBlock;
    }
}
