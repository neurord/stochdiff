package org.catacomb.druid.swing;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.catacomb.interlish.structure.Colored;
import org.catacomb.interlish.structure.Named;


public class CheckListCellRenderer extends JPanel implements ListCellRenderer {
    private static final long serialVersionUID = 1L;


    private JCheckBox checkBox;

    private JLabel label;
    Color csel = new Color(0xd6, 0xd6, 0Xcc);
    JPanel colorPanel;


    @SuppressWarnings("unused")
    public CheckListCellRenderer(ListCellRenderer renderer) {
        //  mainRenderer = renderer;
        setLayout(new BorderLayout(2, 2));
        setOpaque(true);
        checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        setLayout(new BorderLayout());
        label = new JLabel("");
        //label.setOpaque(true);

        label.setFont(new Font("sansserif", Font.PLAIN, 12));

        add("West", checkBox);
        add("Center", label);


        colorPanel = new JPanel();
        colorPanel.setPreferredSize(new Dimension(12, 12));
        colorPanel.setMinimumSize(new Dimension(12, 12));
        colorPanel.setMaximumSize(new Dimension(12, 12));
        Color borderC = new Color(0xa0a0a0);


        colorPanel.setBorder(BorderFactory.createLineBorder(borderC));

        JPanel jp = new JPanel();
        jp.setLayout(new GridLayout(1, 1, 4, 4));
        jp.add(colorPanel);
        jp.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        add("East", jp);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        ToggleItem ti = (ToggleItem)value;
        checkBox.setSelected(ti.isOn());

        Object refobj = ti.getRef();
        if (refobj instanceof Named) {
            label.setText(((Named)refobj).getName());
        } else {
            label.setText(ti.getRef().toString());
        }

        Color cbg = (isSelected ? csel : list.getBackground());
        //  Color cfg = (isSelected ? list.getSelectionForeground() : list.getForeground());

        setBackground(cbg);
        //label.setBackground(cbg);

        if (value instanceof Colored) {
            Color c = ((Colored)value).getColor();
            if (c != null) {
                colorPanel.setBackground(c);
            }
        } else {
            // E.info("note a colored obj... " + value + " " + value.getClass().getName());
        }


        return this;
    }
}