package org.catacomb.druid.swing;


import org.catacomb.druid.event.LabelActor;
import org.catacomb.icon.IconLoader;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.MouseActor;
import org.catacomb.report.E;


import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JButton;

import java.awt.Color;


public abstract class DBaseButton extends JButton implements DComponent {

    protected static final Font plainfont = null;
    protected static final Font boldfont = null;
    protected String label;
    String actionCommand;
    MouseActor mouseActor;
    LabelActor lact;

    public void setLabelText(String s) {
        label = s;
        setText(s);
    }

    public void setActionCommand(String s) {
        actionCommand = s;
    }

    public void setLabelActor(LabelActor bl) {
        lact = bl;
    }

    public void deliverAction(boolean b) {
        if (lact != null) {
            lact.labelAction(actionCommand, b);
        }
    }

    public void setMouseActor(MouseActor ma) {
        addMouseListener(new DMouseRelay(ma));
    }

    public void setPlainFont() {
        setFont(new Font("sansserif", Font.PLAIN, 12));
    }


    public void setBoldFont() {
        setFont(new Font("sansserif", Font.BOLD, 12));
    }

    public abstract void setBg(Color c);

    public abstract void setFg(Color c);


    public void setRolloverPolicy(int inorm, int ihover) {
        E.missing();
    }

    public void setPadding(int p) {
        setPadding(p, p, p, p);

    }

    public void setPadding(int pl, int pr, int pt, int pb) {
        E.missing();
    }

    public void setIconSource(String s) {
        Icon icon = IconLoader.getImageIcon(s);
        setIcon(icon);
    }

    public abstract void suggest();

    public abstract void deSuggest();



}
