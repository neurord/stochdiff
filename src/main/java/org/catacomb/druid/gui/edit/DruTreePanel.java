package org.catacomb.druid.gui.edit;


import org.catacomb.be.StringIdentifiable;
import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.gui.base.DummyTree;
import org.catacomb.druid.swing.DScrollPane;
import org.catacomb.druid.swing.DTree;
import org.catacomb.druid.util.tree.RelationNode;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.awt.Color;
import java.util.ArrayList;


public class DruTreePanel extends DruGCPanel implements TreeDisplay,
    TreeChangeReporter, LabelActor, SelectionActor,
    TreeExplorer, SelectionSource {

    static final long serialVersionUID = 1001;

    DTree dTree;
    DruChoice pivotChoice;
    DScrollPane scrollPane;

    Tree tree;

    SelectionActor selectionActor;
    boolean dropEvents;

    private TreeProvider treeProvider;

    private String selectionType;

    private String pathToOpen;
    private boolean pathRequired;


    private String lastSelection;


    private boolean defaultRootVisibility;


    public DruTreePanel() {
        super();

        dTree = new DTree();
        scrollPane = new DScrollPane(dTree);
        setSingle();
        addDComponent(scrollPane);
        scrollPane.setScrollSize(180, 240);
        selectionType = SpecialStrings.NONE_STRING;
        dTree.setSelectionActor(this);
        setTree(new DummyTree());
    }


    public void setRootVisibility(boolean b) {
        defaultRootVisibility = b;
    }

    public void setBg(Color c) {
        dTree.setBg(c);
        super.setBg(c);
    }


    public void clear() {
        tree = null;
        dTree.clear();
    }


    public void setTree(Tree tr) {
        if (tr != null) {
            tree = tr;
            tree.setTreeChangeReporter(this);
            dTree.setTree(tree);

            if (pivotChoice != null && tree instanceof PivotedTree) {
                pivotChoice.setOptions(((PivotedTree)tree).getPivotNames());
            }

            int ipol = tree.getRootPolicy();
            if (ipol == Tree.HIDE_ROOT) {
                dTree.setRootVisible(false);

            } else if (ipol == Tree.SHOW_ROOT) {
                dTree.setRootVisible(true);

            } else {
                dTree.setRootVisible(defaultRootVisibility);
            }
        }

        if (pathToOpen != null) {
            ensureVisible(pathToOpen, pathRequired);
            pathToOpen = null;
        }
    }


    public void setPivotChoice(DruChoice pc) {
        pivotChoice = pc;

        E.missing("should action connect pivot choice");
//      pivotChoice.setLabelActor(this);
        E.missing("cant add to toolbar...");
        // addToToolbar(pivotChoice);
    }


    public void labelAction(String s, boolean b) {
        // for the repivot actions;
        if (tree instanceof PivotedTree) {
            ((PivotedTree)tree).repivot(s);
            treeModified();
        }
    }


    public void setSelected(String s) {
        //  ensureVisible(s);
        checkIsSelected(s);
    }

    public void selectionAction(Object oselin, String sidin) {
        Object osel = oselin;
        String sid = sidin;
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

        if (dropEvents) {

        } else {
            if (osel instanceof RelationNode) {
                RelationNode rnode = (RelationNode)osel;
                osel = rnode.getPeer();
            }
            if (selectionActor == null) {
                if (osel instanceof StringIdentifiable) {
                    sid = ((StringIdentifiable)osel).getStringIdentifier();

                } else if (osel instanceof TreeNode) {
                    sid = getSlashPath((TreeNode)osel);
                }

                lastSelection = sid;

                valueChange(sid);
            } else {
                selectionActor.selectionAction(osel, sid);
            }
        }
    }




    private String getSlashPath(TreeNode tn) {
        String ret = tn.toString();
        Object p = tn.getParent();
        if (p instanceof TreeNode) {
            ret = getSlashPath((TreeNode)p) + "/" + ret;
        }
        return ret;
    }



    public void setSelectionActor(SelectionActor sact) {
        selectionActor = sact;
    }


    public void treeModified() {
        dropEvents = true;
        dTree.treeModified();
        //dTree.setTree(tree);
        dropEvents = false;
    }



    public void showNewItem(Object[] pathToChild) {
        dropEvents = true;
        dTree.treeModified();
        //dTree.setTree(tree);

        // dumpptc(pathToChild);

        dTree.dTreeExpandPath(pathToChild);
        dropEvents = false;
    }


    @SuppressWarnings("unused")
    private void dumpptc(Object[] oa) {
        for (int i = 0; i < oa.length; i++) {
            Object obj = oa[i];
            E.info("item " + i + "   " + obj + " " + obj.getClass().getName());
        }
    }



    public void setTreeProvider(TreeProvider provider) {
        treeProvider = provider;
        treeProvider.setTreeExplorer(this);


    }



    public void setMenu(DruMenu drum) {
        dTree.setMenu(drum.getGUIPeer());

        // druMenu = drum;
    }



    public String getSelectionType() {
        return selectionType;
    }

    public void enableDrag() {
        dTree.enableDrag();

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


    public void nodeAddedUnder(TreeNode parent, TreeNode child) {
        //    E.info("dru tree should be expanding path to " + parent);
        showNewItem(pathTo(parent));
    }



    public void nodeRemoved(TreeNode parent, TreeNode child) {
        showNewItem(pathTo(parent));
    }

    public void ensureVisible(String sfc) {
        ensureVisible(sfc, true);
    }


    public void expandPath(Object[] oa) {
        dTree.dTreeExpandPath(oa);
    }


    public void ensureVisible(String sfc, boolean breq) {
        Object[] oa = tree.getObjectPath(sfc, breq);
        if (oa != null) {
            Object[] oap = new Object[oa.length-1];
            for (int i = 0; i < oap.length; i++) {
                oap[i] = oa[i];
            }
            dTree.dTreeExpandPath(oap);

        } else {
            if (breq) {
                E.shortWarning("cant match " + sfc + " to a tree path in " + tree);
            }
        }
    }

    public void setPathToOpen(String sfc) {
        setPathToOpen(sfc, true);
    }

    public void setPathToOpen(String sfc, boolean breq) {
        pathToOpen = sfc;
        pathRequired = breq;
    }


    public void checkIsSelected(String pth) {
        dropEvents = true;

        if (pth != null && !(pth.equals(lastSelection))) {
            //    E.info("Time to update selection in tree from " + lastSelection + " to " + pth);
            lastSelection = pth;
            Object[] oa = tree.getObjectPath(pth, true);
            if (oa == null) {
                E.warning("null object path for " + pth);
            } else {
                dTree.setSelected(oa);
            }
        }

        dropEvents = false;
    }


    public String getSelection() {
        return lastSelection;
    }


}
