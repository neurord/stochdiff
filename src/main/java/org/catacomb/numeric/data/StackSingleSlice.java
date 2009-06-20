package org.catacomb.numeric.data;

import java.lang.reflect.Field;


public abstract class StackSingleSlice extends StackSlice {



    public StackSingleSlice(BlockStack bs, String fnm, Field f, String u, String t) {
        super(bs, fnm, f, u, t);
    }


    public int getChildCount() {
        return 0;
    }


    public Object getChild(int index) {
        return null;
    }


    public int getIndexOfChild(Object child) {
        return 0;
    }

    public boolean isLeaf() {
        return true;
    }


    void clearCache() {

    }



}
