package org.catacomb.druid.swing;

import org.catacomb.interlish.structure.Tree;
import org.catacomb.interlish.structure.TreeNode;
import org.catacomb.report.E;

import java.util.ArrayList;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;



public class DTreeModel implements TreeModel {

    Tree tree;


    ArrayList<TreeModelListener> treeModelListeners;


    public DTreeModel(Tree t) {
        tree = t;
        treeModelListeners = new ArrayList<TreeModelListener>();
    }




    public Object getRoot() {
        return tree.getRoot();
    }

    public Tree getTree() {
        return tree;
    }

    public Object getChild(Object parent, int index) {
        return ((TreeNode)parent).getChild(index);
    }


    public int getChildCount(Object parent) {
        return ((TreeNode)parent).getChildCount();
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((TreeNode)parent).getIndexOfChild(child);
    }


    public boolean isLeaf(Object node) {

        return (node instanceof TreeNode) && ((TreeNode)node).isLeaf();
    }




    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("val for path changed " + path);
    }



    // if change tree, set the new root and then call this with the old one;
    protected void fireTreeStructureChanged(Object oldRoot) {

        Object[] args = {oldRoot};
        TreeModelEvent e = new TreeModelEvent(this, args);

        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }


    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }




    public void treeModified() {
        E.info("firing a tree modified change....");
        fireTreeStructureChanged(tree.getRoot());
    }



}
