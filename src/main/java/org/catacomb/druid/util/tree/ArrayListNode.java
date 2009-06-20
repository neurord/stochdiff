package org.catacomb.druid.util.tree;


import org.catacomb.interlish.structure.TreeNode;

import java.util.ArrayList;


public class ArrayListNode implements TreeNode {


    public ArrayList<TreeNode> children;

    String name;

    Object parent;

    public ArrayListNode(Object p, String sn) {
        name = sn;
        children = new ArrayList<TreeNode>();
        parent = p;
    }


    public Object getParent() {
        return parent;
    }

    public String toString() {
        return name;
    }



    public void setChildren(ArrayList<? extends TreeNode> arl) {
        children.clear();
        children.addAll(arl);
    }


    public void clearChildren() {
        children.clear();
    }


    public void addChild(ArrayListNode arn) {
        children.add(arn);
    }


    public void removeChild(ArrayListNode arn) {
        children.remove(arn);
    }


    public boolean hasChildren() {
        return (!(children.isEmpty()));
    }



    public int getChildCount() {
        return children.size();
    }

    public Object getChild(int index) {
        return children.get(index);
    }

    public int getIndexOfChild(Object child) {
        return children.indexOf(child);
    }

    public boolean isLeaf() {
        return (children == null || children.size() == 0);
    }

}
