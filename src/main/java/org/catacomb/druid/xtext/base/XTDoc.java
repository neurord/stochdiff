package org.catacomb.druid.xtext.base;

import org.catacomb.druid.xtext.data.PageDataStore;
import org.catacomb.interlish.structure.TreeNode;



public class XTDoc implements TreeNode {

    String title;
    ContainerBlock rootBlock;
    PageDataStore pdStore;
    DocStore docStore;

    public XTDoc(DocStore ds, String s, ContainerBlock rb) {
        docStore = ds;
        title = s;
        rootBlock = rb;
        pdStore = new PageDataStore(ds);
    }


    public Object getParent() {
        return docStore;
    }


    public String toString() {
        return title;
    }

    public String getTitle() {
        return title;
    }

    public ContainerBlock getRootBlock() {
        return rootBlock;
    }

    public PageDataStore getPageDataStore() {
        return pdStore;
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

}
