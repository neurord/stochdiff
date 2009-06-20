package org.catacomb.druid.swing;

import javax.swing.JPanel;
import java.awt.Component;
import javax.swing.tree.*;
import java.awt.BorderLayout;

import javax.swing.JTree;


public class CheckTreeRenderer extends JPanel implements TreeCellRenderer {
    private static final long serialVersionUID = 1L;

    private CheckTreeSelectionModel selectionModel;
    private TreeCellRenderer delegate;
    private TristateCheckBox checkBox = new TristateCheckBox();


    public CheckTreeRenderer(TreeCellRenderer delegate, CheckTreeSelectionModel selectionModel) {
        this.delegate = delegate;
        this.selectionModel = selectionModel;
        setLayout(new BorderLayout());
        setOpaque(false);
        checkBox.setOpaque(false);
    }


    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component renderer = delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        TreePath path = tree.getPathForRow(row);
        if (path != null) {
            if (selectionModel.isPathSelected(path, true))
                checkBox.setBooleanState(Boolean.TRUE);
            else
                checkBox.setBooleanState(selectionModel.isPartiallySelected(path) ? null : Boolean.FALSE);
        }
        removeAll();
        add(checkBox, BorderLayout.WEST);
        add(renderer, BorderLayout.CENTER);
        return this;
    }
}
