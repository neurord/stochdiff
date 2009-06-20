package org.catacomb.druid.xtext.base;

import org.catacomb.report.E;

import java.util.ArrayList;

public class ContainerBlock extends Block {

    public ArrayList<Block> items;


    DownBlock downBlock;
    UpBlock upBlock;



    public ContainerBlock() {
        items = new ArrayList<Block>();
    }


    public void addBlock(Block bw) {
        items.add(bw);
        bw.setParent(this);
    }

    public ArrayList<Block> getBlocks() {
        return items;
    }



    public void link() {
        DownBlock db = new DownBlock(this);
        setDownBlock(db);
        setNext(db);

        Block bcur = db;
        for (Block b : getBlocks()) {
            bcur.setNext(b);

            if (b instanceof TextBlock) {
                bcur = b;

            } else if (b instanceof ContainerBlock) {
                ContainerBlock cb = (ContainerBlock)b;
                cb.link();
                bcur = cb.getUpBlock();

            } else {
                E.error("wrong block type " + b);
            }
        }

        UpBlock bup = new UpBlock(this);
        setUpBlock(bup);
        bcur.setNext(bup);
    }




    public DownBlock getDownBlock() {
        return downBlock;
    }



    public void setDownBlock(DownBlock downBlock) {
        this.downBlock = downBlock;
    }

    public UpBlock getUpBlock() {
        return upBlock;
    }



    public void setUpBlock(UpBlock upBlock) {
        this.upBlock = upBlock;
    }


    public static ContainerBlock newEmptyText() {
        ContainerBlock cb = new ContainerBlock();
        cb.addBlock(new WordBlock("hello"));
        cb.link();
        return cb;
    }

}
