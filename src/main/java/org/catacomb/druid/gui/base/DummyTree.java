package org.catacomb.druid.gui.base;

import org.catacomb.interlish.structure.Tree;
import org.catacomb.interlish.structure.TreeChangeReporter;
import org.catacomb.interlish.structure.TreeNode;


public class DummyTree implements Tree, TreeNode {



    public void setTreeChangeReporter(TreeChangeReporter tcr) {

    }


    public TreeNode getRoot() {
        return this;
    }


    public String toString() {
        return "dummy tree";
    }

    public int getRootPolicy() {
        return Tree.HIDE_ROOT;
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

    public Object getParent() {
        return null;
    }


    public Object[] getObjectPath(String s, boolean b) {
        return null;
    }
}
