package org.catacomb.druid.swing;


// for the laf
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalComboBoxButton;
import javax.swing.plaf.metal.MetalComboBoxUI;




public class DComboBoxUI extends MetalComboBoxUI implements Icon {



    DComboBoxUI() {
        super();
    }

    public static ComponentUI createUI(JComponent jcomponent) {
        return (new DComboBoxUI());
    }


    protected JButton createArrowButton() {

        System.out.println("creating the arrow button!!!!!!!!!!!!!!!");

        JButton button = new MetalComboBoxButton(comboBox,
                this,
                comboBox.isEditable(),
                currentValuePane,
                listBox);
        button.setMargin(new Insets(0, 1, 1, 3));

        return button;
    }



    public void paintIcon(Component c, Graphics g, int x, int y) {
        JComponent component = (JComponent)c;
        int iconWidth = getIconWidth();

        g.translate(x, y);

        g.setColor(component.isEnabled() ?  Color.gray : Color.blue);

        g.drawLine(0, 0, iconWidth - 1, 0);
        g.drawLine(1, 1, 1 + (iconWidth - 3), 1);
        g.drawLine(3, 3, 3 + (iconWidth - 7), 3);
        g.drawLine(4, 4, 4 + (iconWidth - 9), 4);

        g.translate(-x, -y);
    }

    /**
      * Created a stub to satisfy the interface.
      */
    public int getIconWidth() {
        return 10;
    }

    /**
     * Created a stub to satisfy the interface.
     */
    public int getIconHeight()  {
        return 5;
    }



}





