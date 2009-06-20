package org.catacomb.druid.gui.edit;


import org.catacomb.druid.gui.base.DummyTree;
import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.druid.swing.DCheckboxTree;
import org.catacomb.druid.swing.DScrollPane;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.util.ArrayList;


public class DruCheckboxTreePanel extends DruGCPanel implements SelectionActor, Consumer, Visible {

    static final long serialVersionUID = 1001;

    DCheckboxTree dTree;
    DScrollPane scrollPane;

    Tree tree;

    String selectionType;


    public DruCheckboxTreePanel() {
        super();

        dTree = new DCheckboxTree();
        scrollPane = new DScrollPane(dTree);
        setBorderLayout(2, 2);
        addDComponent(scrollPane, DBorderLayout.CENTER);
        scrollPane.setScrollSize(180, 240);
        setTree(new DummyTree());
    }


    public void clear() {
        tree = null;
        dTree.clear();
    }


    public void setTree(Tree tr) {
        if (tr != null) {
            tree = tr;
            dTree.setTree(tree);

            dTree.setSelectionActor(this);

            int ipol = tree.getRootPolicy();
            if (ipol == Tree.HIDE_ROOT) {
                dTree.setRootVisible(false);

            } else if (ipol == Tree.SHOW_ROOT) {
                dTree.setRootVisible(true);

            } else {
                dTree.setRootVisible(true);
            }
        }
    }



    public void selectionAction(Object osel, String sid) {
        if (osel == null) {
            return; // POSERR;
        }

        if (osel instanceof TreeNode) {
            if (((TreeNode)osel).isLeaf()) {
                selectionType = "leaf";
            } else {
                selectionType = "branch";
            }
        } else {
            E.warning("non tree node in tree selection " + osel);
            selectionType = SpecialStrings.NONE_STRING;
        }


    }


    /*
    private String getSlashPath(TreeNode tn) {
       String ret = tn.toString();
       Object p = tn.getParent();
       if (p instanceof TreeNode) {
          ret = getSlashPath((TreeNode)p) + "/" + ret;
       }
       return ret;
    }
    */


    public void showNewItem(Object[] pathToChild) {

        dTree.setTree(tree);
        // dumpptc(pathToChild);

        dTree.dTreeExpandPath(pathToChild);

    }


    /*
    private void dumpptc(Object[] oa) {
       for (int i = 0; i < oa.length; i++) {
          Object obj = oa[i];
          E.info("item " + i + "   " + obj + " " + obj.getClass().getName());
       }
    }
    */


    public void setMenu(DruMenu drum) {
        dTree.setMenu(drum.getGUIPeer());

        // druMenu = drum;
    }



    public String getSelectionType() {
        return selectionType;
    }


    private Object[] pathTo(TreeNode tn) {
        ArrayList<Object> al = new ArrayList<Object>();

        al.add(tn);

        Object obj = tn.getParent();
        while (obj != null) {
            al.add(0, obj);
            if (obj instanceof TreeNode) {
                obj = ((TreeNode)obj).getParent();
            } else {
                obj = null;
            }
        }
        return al.toArray();
    }


    public String[] getMultiStringSelection() {
        return dTree.getMultiStringSelection();
    }


    public String[] getExpandedMultiStringSelection() {
        return dTree.getExpandedMultiStringSelection();
    }



    public void setSelected(String[] content) {
        dTree.setSelected(content);
    }


    public void ensureVisible(TreeNode tn) {
        if (tn != null) {
            dTree.ensureVisible(pathTo(tn));
        }
    }



}
