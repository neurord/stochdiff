package org.catacomb.druid.swing;

import org.catacomb.druid.gui.base.DummyTree;
import org.catacomb.druid.swing.ui.DruidTreeUI;
import org.catacomb.interlish.structure.SelectionActor;
import org.catacomb.interlish.structure.Tree;
import org.catacomb.report.E;


import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.util.ArrayList;


public class DCheckboxTree extends JTree implements TreeSelectionListener, MouseListener {

    static final long serialVersionUID = 1001;


    public SelectionActor selectionActor;


    DTreeModel dTreeModel;
    DMenu dMenu;

    boolean p_doneMouseListener;


    ToggleItem[] toggleItems;
    HashMap<String, ToggleItem> tiHM;


    private CheckTreeSelectionModel selectionModel;
    int hotspot = new JCheckBox().getPreferredSize().width;



    public DCheckboxTree() {
        super();
        p_doneMouseListener = false;

        setForeground(Color.black);
        setUI(new DruidTreeUI());
        tiHM = new HashMap<String, ToggleItem>();
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        TCRenderer renderer = new TCRenderer();
        setCellRenderer(renderer);
        addTreeSelectionListener(this);
        setTree(new DummyTree());
    }



    public void setSelectionActor(SelectionActor sa) {
        selectionActor = sa;
    }


    public void setTree(Tree tree) {
        dTreeModel = new DTreeModel(tree);
        setModel(dTreeModel);

        selectionModel = new CheckTreeSelectionModel(dTreeModel);
        setCellRenderer(new CheckTreeRenderer(new TCRenderer(), selectionModel));
        addMouseListener(this);
        selectionModel.addTreeSelectionListener(this);
    }



    public void mouseClicked(MouseEvent me) {
        TreePath path = getPathForLocation(me.getX(), me.getY());
        if (path == null) {
            return;
        }

        if (me.getX() > getPathBounds(path).x + hotspot) {
            return;
        }

        boolean selected = selectionModel.isPathSelected(path, true);
        selectionModel.removeTreeSelectionListener(this);

        try {
            if (selected) {
                selectionModel.removeSelectionPath(path);
            } else {

                //   E.info("cbt adding " + path);

                selectionModel.addSelectionPath(path);

//           for (TreePath tp : selectionModel.getSelectionPaths()) {
//               E.info("currently selected "  + tp);
//            }

            }
        } finally {
            selectionModel.addTreeSelectionListener(this);
            treeDidChange();
        }
    }


    public CheckTreeSelectionModel getSelectionModel() {
        if (selectionModel == null) {
            selectionModel = new CheckTreeSelectionModel(new DTreeModel(new EmptyTree()));
        }
        return selectionModel;
    }


    public void valueChanged(TreeSelectionEvent e) {
        treeDidChange();
    }


    public void clear() {
        setModel(new DTreeModel(new EmptyTree()));
    }


    public void dTreeExpandPath(Object[] oa) {
        TreePath tp = new TreePath(oa);
        expandPath(tp);
    }


    public void ensureVisible(Object[] oa) {
        if (oa.length > 1) {
            Object[] oas = new Object[oa.length - 1];
            for (int i = 0; i < oa.length - 1; i++) {
                oas[i] = oa[i];
            }
            dTreeExpandPath(oas);
        }
        // POSERR sloppy to do both - could be stricter in whether to include end
        // leaf or not;
        dTreeExpandPath(oa);
    }



    public void setMenu(DMenu dm) {
        if (!p_doneMouseListener) {
            addMouseListener(this);
            p_doneMouseListener = true;
        }
        dMenu = dm;

    }


    public void mousePressed(MouseEvent e) {
        int modif = e.getModifiers();

        if (dMenu != null) {
            dMenu.preShowSync();
            if ((modif & InputEvent.BUTTON3_MASK) != 0) {
                dMenu.getPopupMenu().show(this, e.getX(), e.getY());
            }
        }
    }



    public void setBg(Color c) {
        setBackground(c);
    }






    public void mouseReleased(MouseEvent e) {
    }



    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }







    private String makeSlashPath(TreePath tp) {
        Object[] oa = tp.getPath();

        StringBuffer sb = new StringBuffer();
        // POSERR start at 0 to include root folder - do we ever want that?
        for (int i = 1; i < oa.length-1; i++) {
            sb.append(oa[i].toString());
            sb.append("/");
        }
        sb.append(oa[oa.length - 1].toString());
        return sb.toString();
    }


    public String[] getMultiStringSelection() {
        TreePath[] tpa = selectionModel.getSelectionPaths();
        String[] ret = new String[tpa.length];
        for (int i = 0; i < tpa.length; i++) {
            ret[i] = makeSlashPath(tpa[i]);
        }
        return ret;
    }

    public String[] getExpandedMultiStringSelection() {
        ArrayList<String> asa = new ArrayList<String>();

        for (TreePath tp : selectionModel.getSelectionPaths()) {
            for (TreePath stp : selectionModel.getDescendantPaths(tp)) {
                asa.add(makeSlashPath(stp));
            }
        }

        return asa.toArray(new String[0]);
    }





    public void addStringSelection(String sp) {
        // E.info("time to select path " + sp);
        Tree tr = dTreeModel.getTree();
        Object[] oa = tr.getObjectPath(sp, true);
        if (oa == null) {
            E.warning("cannot get path for " + sp);
        } else {
            TreePath tp = new TreePath(oa);
            TreePath[] tpa = { tp };
            selectionModel.addSelectionPaths(tpa);
        }
    }


    /*
    private String printPath(Object[] oa) {
       StringBuffer sb = new StringBuffer();
       sb.append("-");
       for (int i = 0; i < oa.length; i++) {
          sb.append(oa[i].toString());
          sb.append("-");
       }
       return sb.toString();
    }
    */

    public void setSelected(String[] content) {
        selectionModel.clearSelection();
        if (content == null) {

        } else {
            for (String sp : content) {
                sp = sp.trim();
                if (sp.length() > 0) {
                    addStringSelection(sp);
                }
            }
        }
    }



}
