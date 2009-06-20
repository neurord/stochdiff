package org.catacomb.druid.xtext.base;

import org.catacomb.druid.xtext.parse.TextSplitter;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.util.ArrayList;


public class DocStore implements Producer, TreeProvider, Tree, TreeNode,
    SelectionActor {


    ArrayList<XTDoc> docs;

    XTDoc dummyDoc;

    static int iti = 100;

    TreeExplorer treeExp;


    DocDisplay docDisplay;

    TreeChangeReporter tcReporter;


    public DocStore() {
        docs = new ArrayList<XTDoc>();
    }


    public String toString() {
        return "pages";
    }


    public void setDocDisplay(DocDisplay dd) {
        docDisplay = dd;
    }


    public XTDoc getDummyDoc(String txt) {
        if (dummyDoc == null) {

            TextSplitter ts = new TextSplitter(txt);

            ContainerBlock rootBlock = ts.makeBlock();

            dummyDoc = new XTDoc(this, "example", rootBlock);
            docs.add(dummyDoc);
            updateTree();

        }
        return dummyDoc;
    }


    public XTDoc getImportDoc(String txt, String name) {
        TextSplitter ts = new TextSplitter(txt);

        ContainerBlock rootBlock = ts.makeBlock();

        XTDoc xtd  = new XTDoc(this, name, rootBlock);
        docs.add(xtd);
        updateTree();

        return xtd;
    }



    public XTDoc nextTextDoc() {
        iti += 1;

        ContainerBlock rb = ContainerBlock.newEmptyText();
        String title = "source_" + iti;

        XTDoc xtd = new XTDoc(this, title, rb);

        docs.add(xtd);
        updateTree();

        return xtd;
    }



    public void updateTree() {
        if (tcReporter != null) {
            E.info("NB should use tcReporter");
        }

        if (treeExp != null) {
            treeExp.treeModified();

            Object[] oa = {this};
            treeExp.showNewItem(oa);

        }
    }


    public void setTreeExplorer(TreeExplorer treeex) {
        treeExp = treeex;
        treeExp.setTree(this);
        treeExp.setSelectionActor(this);
        updateTree();
    }


    public void setTreeChangeReporter(TreeChangeReporter tcr) {
        tcReporter = tcr;
    }



    public Object getParent() {
        return null;
    }


    public TreeNode getRoot() {
        return this;
    }


    public int getRootPolicy() {
        return Tree.SHOW_ROOT;
    }


    public int getChildCount() {
        return docs.size();
    }


    public Object getChild(int index) {
        return docs.get(index);
    }


    public int getIndexOfChild(Object child) {
        return docs.indexOf(child);
    }


    public boolean isLeaf() {
        return false;
    }


    public XTDoc getDocByID(String s) {
        XTDoc ret = null;
        for (XTDoc xtd : docs) {
            if (s.equals(xtd.toString())) {
                ret = xtd;
                break;
            }
        }
        return ret;
    }



    public void selectionAction(Object obj, String id) {
        if (obj instanceof XTDoc) {
            if (docDisplay != null) {
                docDisplay.showDoc((XTDoc)obj);
            }

        } else {
            E.info("unexpected type " + obj);

            XTDoc  xtd = getDocByID(id);
            if (docDisplay != null) {
                docDisplay.showDoc(xtd);
            }
        }

    }


    public void pageDataChanged() {
        if (docDisplay != null) {
            docDisplay.repaintPageData();
        }

    }


    public Object[] getObjectPath(String s, boolean b) {
        E.missing();
        return null;
    }





}
