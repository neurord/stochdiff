package org.catacomb.interlish.structure;




/*
 lets you get the root and its nodes/leaves
 possibly each node can be decorated in a variety of ways
 should allow drag and drop etc within the tree

*/



public interface Tree {


    int SHOW_ROOT = 1;
    int HIDE_ROOT = 2;
    int AUTO_ROOT = 3;


    // key thing here is to scale nicely - don't expand more than
    // necessary and paginate long lists


    TreeNode getRoot();

    int getRootPolicy();


    void setTreeChangeReporter(TreeChangeReporter tcr);

    public Object[] getObjectPath(String s, boolean breq);



}
