
package org.catacomb.druid.swing.ui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;

import org.catacomb.icon.IconLoader;


public final class DruidTreeUI extends BasicTreeUI   {

    Icon colIcon;
    Icon expIcon;




    public DruidTreeUI() {
        super();

        colIcon = IconLoader.createImageIcon("collapsed.gif");
        expIcon = IconLoader.createImageIcon("expanded.gif");

        setCollapsedIcon(colIcon);
    }


    public Icon getCollapsedIcon() {
        return colIcon;
    }

    public Icon getExpandedIcon() {
        return expIcon;
    }


    public static ComponentUI createUI(JComponent jcomponent) {
        return (new DruidTreeUI());
    }


    public void installUI(JComponent jcomponent) {
        super.installUI(jcomponent);
    }


    public void uninstallUI(JComponent jcomponent) {
        super.uninstallUI(jcomponent);
    }


    protected void paintVerticalLine(Graphics g, JComponent jcomponent,
                                     int i, int j ,int k) {
        g.setColor(Color.gray);
        drawDashedVerticalLine(g, i , j , k);
    }


    protected void paintHorizontalLine(Graphics g, JComponent jcomponent,
                                       int i, int j, int k) {
        g.setColor(Color.gray);
        drawDashedHorizontalLine(g, i , j , k);
    }



}
