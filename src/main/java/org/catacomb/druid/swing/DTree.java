
package org.catacomb.druid.swing;

import java.awt.Color;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.catacomb.druid.swing.dnd.InternalTransferHandler;
import org.catacomb.druid.swing.ui.DruidTreeUI;
import org.catacomb.interlish.structure.SelectionActor;
import org.catacomb.interlish.structure.Tree;


public class DTree extends JTree
    implements TreeSelectionListener, MouseListener {
    static final long serialVersionUID = 1001;


    public SelectionActor selectionActor;


    DTreeDragSource dTreeDragSource;
    DTreeDropTarget dTreeDropTarget;

    DTreeModel dTreeModel;
    DMenu  dMenu;

    boolean p_doneMouseListener;

    TCRenderer renderer;

    public DTree() {
        super();
        p_doneMouseListener = false;


        setForeground(Color.black);

        setUI(new DruidTreeUI());

        init();

        addTreeSelectionListener(this);
    }



    public void setSelectionActor(SelectionActor sa) {
        selectionActor = sa;
    }


    public void valueChanged(TreeSelectionEvent e) {
        Object obj = getLastSelectedPathComponent();

        if (selectionActor != null) {
            selectionActor.selectionAction(obj, "DTree");
        }

    }



    public void setBg(Color c) {
        setBackground(c);
        renderer.setBackgroundNonSelectionColor(c);
    }


    public void init() {
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        renderer = new TCRenderer();
        setCellRenderer(renderer);
    }


    public void setTree(Tree tree) {
        dTreeModel = new DTreeModel(tree);
        setModel(dTreeModel);
    }

    public void treeModified() {
        dTreeModel.treeModified();
    }


    public void clear() {
        setModel(new DTreeModel(new EmptyTree()));
    }


    public void dTreeExpandPath(Object[] oa) {
        TreePath tp = new TreePath(oa);
        expandPath(tp);
    }


    public void setSelected(Object[] oa) {
        TreePath tp = new TreePath(oa);
        setSelectionPath(tp);
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


    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}




    public void enableDrag() {
        if (dTreeDragSource == null) {
            dTreeDragSource = new DTreeDragSource(this, DnDConstants.ACTION_COPY_OR_MOVE);
        }

        if (dTreeDropTarget == null) {
            dTreeDropTarget = new DTreeDropTarget(this);
        }

        setTransferHandler(new InternalTransferHandler());

        setDragEnabled(true);
    }


    public void ensureVisible(String sfc) {
    }


}
