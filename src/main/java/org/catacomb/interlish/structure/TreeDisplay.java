package org.catacomb.interlish.structure;





public interface TreeDisplay {


    void setTree(Tree t);


    void treeModified();


    void setSelectionActor(SelectionActor  sa);


    void showNewItem(Object[] pathTo);

    void setSelected(String s);

    void clear();

}
