package org.catacomb.interlish.structure;




public interface TreeNode {


    Object getParent();

    int getChildCount();

    Object getChild(int index);

    int getIndexOfChild(Object child);

    boolean isLeaf();


}
