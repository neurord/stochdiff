package org.catacomb.util;

import org.catacomb.interlish.structure.TreeNode;


public class StringTreeLeaf implements TreeNode {

    String label;

    TreeNode parent;

    public StringTreeLeaf(String lab) {
        label = lab;
    }

    public String toString() {
        return label;
    }


    public void setParent(TreeNode p) {
        parent = p;
    }

    public Object getParent() {
        return parent;
    }

    public int getChildCount() {
        return 0;
    }

    public Object getChild(int index) {
        return null;
    }

    public int getIndexOfChild(Object child) {
        return -1;
    }

    public boolean isLeaf() {
        return true;
    }

    public String getLabel() {
        return label;
    }

}
