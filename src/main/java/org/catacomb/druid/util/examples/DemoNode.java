package org.catacomb.druid.util.examples;



import org.catacomb.interlish.structure.TreeNode;

import java.util.ArrayList;



public class DemoNode implements TreeNode {

    public String name;

    public ArrayList<TreeNode> children;

    Object parent;

    public DemoNode(Object p) {
        this(p, "root");
    }

    public DemoNode(Object p, String s) {
        name = s;
        parent = p;
    }


    public DemoNode(String s, int level) {
        name = s;
        children = new ArrayList<TreeNode>();
        children.add(new DemoNode("child-a-" + level));
        children.add(new DemoNode("child-b-" + level));
        children.add(new DemoNode("child-c-" + level));

        if (level > 0) {
            children.add(new DemoNode("folder-" + level, level - 1));
        }
    }


    public Object getParent() {
        return parent;
    }



    public String toString() {
        return name;
    }


    public int getChildCount() {
        int nch = 0;
        if (children != null) {
            nch = children.size();
        }
        return nch;
    }

    public Object getChild(int index) {
        return children.get(index);
    }

    public int getIndexOfChild(Object child) {
        return children.indexOf(child);
    }


    public boolean isLeaf() {
        boolean isleaf = true;
        if (children != null && children.size() > 0) {
            isleaf = false;
        }
        return isleaf;
    }



}
