package org.catacomb.druid.build;

import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;




public class TreeController implements Controller, GUISourced, SelectionActor {


    Tree tree;

    @IOPoint(xid="tree")
    public TreeDisplay treeDisplay;



    public TreeController() {

    }


    public String getGUISources() {
        return "tree";   // POSERR used to be settalbe
    }



    public void attached() {
        treeDisplay.setSelectionActor(this);

        if (tree != null) {
            treeDisplay.setTree(tree);
        }
    }




    public void setTree(Tree xtree) {
        tree = xtree;
        if (treeDisplay == null) {
            E.error("no display for tree ");
        } else {
            treeDisplay.setTree(tree);
        }
    }

    @SuppressWarnings("unused")
    public void repivot(String s) {
        E.missing();
    }


    public void treeModified() {
        treeDisplay.treeModified();
    }


    public void selectionAction(Object obj, String id) {
        E.missing("respond to selection action?");
    }


    public void showNewItem(Object[] pathTo) {
        treeDisplay.showNewItem(pathTo);

    }


    /*
    public void addToSelectedFolder() {
       System.out.println("Tree handler - add to selected folder");
    }
    */



}
