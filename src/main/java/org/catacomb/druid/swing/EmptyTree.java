package org.catacomb.druid.swing;

import org.catacomb.interlish.structure.Tree;
import org.catacomb.interlish.structure.TreeChangeReporter;
import org.catacomb.interlish.structure.TreeNode;


public class EmptyTree implements Tree, TreeNode {

    public TreeNode getRoot() {
        return this;
    }


    public int getRootPolicy() {
        return HIDE_ROOT;
    }


    public void setTreeChangeReporter(TreeChangeReporter tcr) {

    }


    public Object getParent() {
        return null;
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


    public Object[] getObjectPath(String s, boolean b) {
        return null;
    }

}
