
package org.catacomb.druid.blocks;


import java.util.ArrayList;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruImageLabelPanel;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.AddableTo;


public class ImageLabel extends Panel implements AddableTo {

    ArrayList<StateImage> stateImages;

    public ImageLabel() {
        stateImages = new ArrayList<StateImage>();
    }

    public void add(Object obj) {
        stateImages.add((StateImage)obj);
    }

    public DruPanel instantiatePanel() {
        return new DruImageLabelPanel();
    }



    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {
        DruImageLabelPanel dilp = (DruImageLabelPanel)drup;
        for (StateImage si : stateImages) {
            dilp.addImage(si.src, si.tag);
        }

    }



}
