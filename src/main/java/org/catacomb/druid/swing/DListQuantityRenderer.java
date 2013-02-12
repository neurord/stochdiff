package org.catacomb.druid.swing;



import org.catacomb.interlish.content.NVPair;
import org.catacomb.report.E;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;


import java.awt.Dimension;

public class DListQuantityRenderer extends JPanel implements DListCellRenderer {

    static final long serialVersionUID = 1001;

    JTextField quantityTF;
    JLabel quantityLabel;
    JLabel nameLabel;
    Color csel = new Color(0xb8, 0xcf, 0xe5);

    public DListQuantityRenderer() {
        //   setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

        setLayout(new BorderLayout(2, 2));

        Font plainfont = new Font("sansserif", Font.PLAIN, 12);

//     quantityTF = new JTextField(12);
//     quantityTF.setEditable(true);
        quantityLabel = new JLabel();
        quantityLabel.setPreferredSize(new Dimension(60, 18));
        nameLabel = new JLabel();
//      nameLabel.setOpaque(true);
        nameLabel.setFont(plainfont);



        add("West", quantityLabel);
        add("Center", nameLabel);


        /*
        Color borderC = new Color(0xa0a0a0);

        colorPanel.setBorder(BorderFactory.createLineBorder(borderC));

        colorLabel.setBorder(BorderFactory.createCompoundBorder(
                                               BorderFactory.createEmptyBorder(4, 4, 4, 4),
                                               BorderFactory.createLineBorder(borderC)));
        */

    }



    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        if (value instanceof NVPair) {
            NVPair nvp = (NVPair)value;
            //  quantityTF.setText(nvp.getSValue());
            quantityLabel.setText(nvp.getSValue());
            nameLabel.setText(nvp.getName());

        } else {
            E.error("cannot render " + value);
        }

        Color cbg = (isSelected ? csel : list.getBackground());
//      Color cfg = (isSelected ? list.getSelectionForeground() : list.getForeground());

        setBackground(cbg);
        nameLabel.setBackground(cbg);
        quantityLabel.setBackground(cbg);
        return this;
    }


}
