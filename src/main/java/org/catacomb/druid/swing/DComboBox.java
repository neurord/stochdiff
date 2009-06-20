
package org.catacomb.druid.swing;


import org.catacomb.druid.event.LabelActor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

public class DComboBox extends JComboBox implements ActionListener {
    static final long serialVersionUID = 1001;

    LabelActor lact;

    public DComboBox(String[] opts) {
        super(opts);

        //Color col = LAF.getBackgroundColor();
        //      System.out.println("panel setting background " + col);

        setUI(new DComboBoxUI());

        //      setFocusPainted(false);
        addActionListener(this);


        //      addMouseListener(new RolloverEffect(this));

    }



    public void actionPerformed(ActionEvent aev) {
        deliverAction(aev.toString(), true);
    }


    public void setLabelActor(LabelActor la) {
        lact = la;
    }


    public void deliverAction(String s, boolean b) {
        if (lact != null) {
            lact.labelAction(s, b);
        }
    }






    /*
    public void addBorder(int l, int r, int t, int b) {
       System.out.println("DPanel adding border " + l + " " + r );

       Border border = BorderFactory.createEmptyBorder(t, l, b, r);
       setBorder(border);
    }
    */


    /*
      setFont(new Font ("sansserif", Font.PLAIN, 12));

      rollover = new RolloverEffect(this);

      addMouseListener(rollover);
      //      setBorder(BorderFactory.createEtchedBorder());

      //      setFont(new Font ("sansserif", Font.PLAIN, 12));
      this.label = lab;
      //      setToolTipText(infoText);


      public void addInnerBorder(Border border) {
      rollover.addInnerBorder(border);
      }


      public void setActionCommand(String s) {
      actionCommand = s;
      }

    */


}

