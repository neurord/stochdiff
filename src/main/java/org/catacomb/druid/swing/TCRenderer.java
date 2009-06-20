package org.catacomb.druid.swing;



import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.catacomb.icon.IconLoader;




public class TCRenderer extends DefaultTreeCellRenderer {
    static final long serialVersionUID = 1001;

    static Icon leafIcon;
    static Icon closedIcon;
    static Icon openIcon;


    static {
        leafIcon = IconLoader.createImageIcon("leaf.gif");
        closedIcon = IconLoader.createImageIcon("closed.gif");
        openIcon = IconLoader.createImageIcon("open.gif");
    }





    public Component getTreeCellRendererComponent(JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean lhasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel,expanded, leaf, row, lhasFocus);

        if (leaf) {
            setIcon(leafIcon);
        } else if (expanded) {
            setIcon(openIcon);
        } else {
            setIcon(closedIcon);
        }


        return this;
    }


}
