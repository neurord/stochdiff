package org.catacomb.druid.gui.edit;


import java.awt.Color;
import java.util.ArrayList;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DBaseButton;
import org.catacomb.druid.swing.DButton;
import org.catacomb.icon.DImageIcon;
import org.catacomb.icon.IconLoader;
import org.catacomb.interlish.structure.Button;
import org.catacomb.interlish.structure.Suggestible;


public class DruButton extends DruGCPanel implements LabelActor, Button, Suggestible {

    static final long serialVersionUID = 1001;

    String label;

    ArrayList<Effect> effects;

    DBaseButton button;




    public DruButton(String lab) {
        this(lab, null);
    }




    public DruButton(String lab, String ac) {
        super();
        label = lab;
        button = makeButton(label);
        setActionCommand(ac);

        setActionMethod(ac);

        addSingleDComponent(button);
        button.setLabelActor(this);

    }


    public void suggest() {

        //  E.error("called suggest...");

        button.suggest();
    }


    public void deSuggest() {
        button.deSuggest();
    }


    public String toString() {
        return ("DruButton  " + label);
    }


    public DBaseButton getButton() {
        return button;
    }




    public void setRolloverPolicy(int inorm, int ihover) {
        button.setRolloverPolicy(inorm, ihover);
    }



    public DBaseButton makeButton(String sl) {
        return new DButton(sl);
    }


    public void setBg(Color c) {
        button.setBg(c);
    }


    public void setFg(Color c) {
        button.setFg(c);
    }



    public String getLabel() {
        return button.getText();
    }


    public void disable() {
        able(false);
    }

    public void enable() {
        able(true);
    }

    public void able(boolean b) {
        button.setEnabled(b);
    }



    public void setBoldFont() {
        button.setBoldFont();
    }


    public void setImage(String iconName) {
        DImageIcon icon = IconLoader.createImageIcon(iconName);
        button.setIcon(icon);
    }


    public void setIconSource(String imgsrc) {
        button.setIconSource(imgsrc);
    }


    public void setLabelText(String s) {
        button.setLabelText(s);
    }


    public void postApply() {
        button.setMouseActor(this);
    }




    public void setActionCommand(String s) {
        button.setActionCommand(s);
    }


    public void applyEffects(boolean b) {
        if (effects != null) {
            for (Effect eff : effects) {
                eff.apply(b);
            }
        }


    }


    public void labelAction(String s, boolean b) {
        exportInfo();

        applyEffects(true);

        action();

    }


    public void addEffect(Effect eff) {
        if (effects == null) {
            effects = new ArrayList<Effect>();
        }
        effects.add(eff);
    }

    public void setEffects(ArrayList<Effect> arl) {
        effects = arl;
    }



    public void setPadding(int padding) {
        button.setPadding(padding);
    }


    public void setPadding(int pl, int pr, int pt, int pb) {
        button.setPadding(pl, pr, pt, pb);
    }

}
