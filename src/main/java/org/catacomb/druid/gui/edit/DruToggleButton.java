package org.catacomb.druid.gui.edit;



import org.catacomb.druid.build.ContingencyGroup;
import org.catacomb.druid.build.GroupContingent;
import org.catacomb.druid.swing.DBaseButton;
import org.catacomb.druid.swing.DToggleButton;
import org.catacomb.icon.DImageIcon;
import org.catacomb.icon.IconLoader;
import org.catacomb.report.E;

import java.awt.Color;


public class DruToggleButton extends DruButton implements GroupContingent {

    static final long serialVersionUID = 1001;

    // String group;
    String value;
    ContingencyGroup contingencyGroup;

    DToggleButton toggleButton;



    public DruToggleButton(String lab, String act) {
        super(lab, act);
    }



    public DBaseButton makeButton(String lab) {
        toggleButton = new DToggleButton(lab);
        return toggleButton;
    }

    public void setToggle(String gr, String va) {
        // group = gr;
        value = va;
    }

    public String getValue() {
        return value;
    }

    public void setBg(Color c) {
        toggleButton.setBg(c);
        toggleButton.applyState();
    }


    public void setInitialValue(boolean b) {
        toggleButton.setState(b);
    }


    public void labelAction(String s, boolean b) {

        exportInfo();

        applyEffects(b);


        unstoredValueChange(b);

        if (contingencyGroup != null && b) {
            contingencyGroup.deselectOthers(this);

            if (contingencyGroup.hasAction()) {
                action(contingencyGroup.getAction(), getValue());
            }
        }
    }



    public void setContingencyGroup(ContingencyGroup cg) {
        contingencyGroup = cg;
        contingencyGroup.add(this);
    }


    public void select() {
        E.info("dtb select ");
        toggleButton.setState(true);
    }


    public void deselect() {
        toggleButton.setState(false);
    }



    public void setOffImage(String iconName) {
        DImageIcon icon = IconLoader.createImageIcon(iconName);
        toggleButton.setOffIcon(icon);
    }


    public void setOnImage(String iconName) {
        DImageIcon icon = IconLoader.createImageIcon(iconName);
        toggleButton.setOnIcon(icon);
    }



    public void setGroupAction(String action) {
        if (contingencyGroup != null) {
            contingencyGroup.setAction(action);
        } else {
            E.warning("no contingency group?");
        }
    }



}
