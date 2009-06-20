
package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JRadioButton;


public final class DRadioButton extends JRadioButton implements
    MouseListener, ItemListener {
    static final long serialVersionUID = 1001;

    String infoText;
    LabelActor bl;

    boolean ignore = false;


    public DRadioButton(String sin, String sact, boolean b) {
        super();
        String s =sin;
        if (s == null) {
            s = "";
        }

        setText(s);
        setActionCommand(sact);

        setSelected(b);
        addItemListener(this);
    }




    public DRadioButton(String s, boolean b, String infoText) {
        super(s, b);
        this.infoText = infoText;
        if (infoText != null) {
            setToolTipText(infoText);
        }
        addMouseListener(this);
        setFont(new Font("sansserif", Font.PLAIN, 12));

    }


    public void setLabelActor(LabelActor bl) {
        this.bl = bl;
    }



    public void deliverLabelAction(String s, boolean b) {
        if (bl != null) {
            bl.labelAction(s, b);
        }
    }



    public void itemStateChanged(ItemEvent iev) {
        if (!ignore) {
            boolean b = isSelected();
            deliverLabelAction(getActionCommand(), b);
        }
    }


    public void  mouseClicked(MouseEvent e) { }
    public void  mousePressed(MouseEvent e) { }
    public void  mouseReleased(MouseEvent e) { }

    public void  mouseEntered(MouseEvent e) {
    }

    public void  mouseExited(MouseEvent e) { }


    public void setState(boolean b) {
        if (b != isSelected()) {
            setSelected(b);
        }
    }

    public boolean getState() {
        return isSelected();
    }

}


