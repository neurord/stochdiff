package org.catacomb.numeric.data;

import java.lang.reflect.Field;

import org.catacomb.interlish.content.BasicTouchTime;
import org.catacomb.interlish.structure.TreeNode;

public abstract class StackSlice implements TreeNode {

    BlockStack blockStack;

    String fieldName;
    String unit;
    String title;
    Field field;


    BasicTouchTime cacheTime;


    public StackSlice(BlockStack bs, String fnm, Field f, String u, String t) {
        blockStack = bs;
        fieldName = fnm;
        field = f;
        unit = u;
        title = t;
        cacheTime = new BasicTouchTime();
    }


    public String getName() {
        return fieldName;
    }

    public String getLabel() {
        return title;
    }

    public String toString() {
        // TODO - does it matter? - should tree be wrapped
        // E.error("shouldnt use to String here");
        return fieldName;
    }


    public String getUnit() {
        return unit;
    }

    public String getTitle() {
        return title;
    }


    public int getNPoint() {
        return blockStack.getSize();
    }


    boolean upToDate() {
        return cacheTime.isAfter(blockStack.getChangeTime());
    }


    public void clear() {
        cacheTime.now();
        clearCache();
    }


    abstract void clearCache();


    int getSize() {
        return (blockStack.getSize());
    }




    public Object getParent() {
        return blockStack;
    }







}
