package org.catacomb.druid.swing;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.catacomb.interlish.structure.ColorMapped;
import org.catacomb.interlish.structure.Colored;
import org.catacomb.interlish.structure.Labelled;
import org.catacomb.report.E;




public class DListColorRenderer extends JPanel implements DListCellRenderer {

    static final long serialVersionUID = 1001;

    JLabel nameLabel;

    JPanel colorPanel;
    // GradientPanel gradientPanel;

    Color csel = new Color(0xe6, 0xe6, 0Xdc);

    JPanel jpl;
//   JPanel jpr;

    public DListColorRenderer() {
        //   setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

        setLayout(new BorderLayout(2, 2));

        Font plainfont = new Font("sansserif", Font.PLAIN, 12);

        colorPanel = new JPanel();
        colorPanel.setPreferredSize(new Dimension(18, 12));
        colorPanel.setMinimumSize(new Dimension(18, 12));
        colorPanel.setMaximumSize(new Dimension(18, 12));

        //  gradientPanel = new GradientPanel(32, 12);



        nameLabel = new JLabel();
//      nameLabel.setOpaque(true);
        nameLabel.setFont(plainfont);


        jpl = new JPanel();
        jpl.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        jpl.add(colorPanel);
        jpl.add(nameLabel);

        //  jpr = new JPanel();
        //  jpr.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        //  jpr.add(gradientPanel);

        add("Center", jpl);
        //  add("East", jpr);


        Color borderC = new Color(0xa0a0a0);

        colorPanel.setBorder(BorderFactory.createLineBorder(borderC));
        /*
              colorLabel.setBorder(BorderFactory.createCompoundBorder(
                                                     BorderFactory.createEmptyBorder(4, 4, 4, 4),
                                                     BorderFactory.createLineBorder(borderC)));
        */

    }



    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {


        String s = "";
        if (value instanceof Labelled) {
            s = ((Labelled)value).getLabel();
        } else {
            s = value.toString();
        }


        nameLabel.setText(s);

        Color cbg = (isSelected ? csel : list.getBackground());
//      Color cfg = (isSelected ? list.getSelectionForeground() : list.getForeground());

        setBackground(cbg);
        jpl.setBackground(cbg);
        //   jpr.setBackground(cbg);

        if (value instanceof Colored) {
            Color c = ((Colored)value).getColor();
            colorPanel.setBackground(c);
        } else {

            E.warning("not a colored list item " + value + " " + value.getClass());
        }

        if (value instanceof ColorMapped) {
            //   ColorTable ct = ((ColorMapped)value).getColorTable();
            E.missing("should make specific renderer for color mapped items");
            //    gradientPanel.setColorTable(ct);
        } else {
            //   gradientPanel.setColorTable(null);
        }


        return this;
    }


}
