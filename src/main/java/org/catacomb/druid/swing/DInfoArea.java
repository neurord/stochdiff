

package org.catacomb.druid.swing;
import org.catacomb.druid.event.LabelActor;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.SwingConstants;


public class DInfoArea extends DPanel {
    static final long serialVersionUID = 1001;

    DButton label;

    CardLayout cardLayout;



    // DScrollPane jsp;


    public DInfoArea(String content, String actionCommand) {

        setLayout(new GridLayout(1, 1));

        label = new DButton(wrap(content));
        label.setActionCommand(actionCommand);

        //      Font finfo = Font.decode("Arial-BOLD-12");

        Font finfo = new Font("SansSerif", Font.PLAIN, 11);


        label.setFont(finfo);

        label.setForeground(new Color(0x606060));

        label.setVerticalAlignment(SwingConstants.TOP);

        label.setPadding(10);


        Color c = LAF.getBackgroundColor();
        setBg(c);
        label.setBg(c);

        add(label);

    }



    /*
    public Dimension getPreferredSize() {
       return new Dimension(150, 40);
    }
    */


    //   public String getText() { return jta.getText(); }

    public String wrap(String s) {
        String stxt = "<html><body><center>[click panel to activate]</center><br>\n<p>" + s + "\n</p></body></html>\n";
        return stxt;

    }

    public void setLabelActor(LabelActor lact) {
        label.setLabelActor(lact);
    }



    public void setCardLayout() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
    }



    public void nextCard() {
        cardLayout.next(this);
    }



    public void showCard(String s) {
        cardLayout.show(this, s);
    }


}







