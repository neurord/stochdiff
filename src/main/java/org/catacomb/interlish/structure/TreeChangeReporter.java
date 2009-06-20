package org.catacomb.interlish.structure;


public interface TreeChangeReporter {


    public void nodeAddedUnder(TreeNode parent, TreeNode child);

    public void nodeRemoved(TreeNode parent, TreeNode child);


}
