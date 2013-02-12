package org.catacomb.druid.gui.base;


import org.catacomb.druid.swing.DLabel;
import org.catacomb.icon.DImageIcon;
import org.catacomb.icon.IconLoader;
import org.catacomb.report.E;


public class DruImageLabelPanel extends DruPanel {

    static final long serialVersionUID = 1001;

    DLabel dLabel;

    String[] tags;
    DImageIcon[] icons;
    int nim;


    public DruImageLabelPanel() {
        super();
        DImageIcon dia = IconLoader.createImageIcon("normal.gif");
        dLabel = new DLabel(dia);
        tags = new String[6];
        icons = new DImageIcon[6];
        nim = 0;
        addSingleDComponent(dLabel);
    }





    public void postApply() {

        if (info != null) {
            dLabel.setMouseActor(this);
        } else {
//        E.warning("not bothering with label action as info is null " + text);
        }
    }



    public void addImage(String src, String tag) {
        tags[nim] = tag;
        icons[nim] = IconLoader.createImageIcon(src);
        if (nim == 0) {
            dLabel.setIcon(icons[nim]);
        }
        nim++;
    }

    public void showImage(String s) {
        boolean done = false;
        for (int i = 0; i < nim; i++) {
            if (tags[i].equals(s)) {
                dLabel.setIcon(icons[i]);
                //  E.info("reset label icon ");
                done = true;
                break;
            }
        }
        if (!done) {
            E.warning("cannot find tag " + s);
        }
    }

}
