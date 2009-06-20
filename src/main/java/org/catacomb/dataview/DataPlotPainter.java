package org.catacomb.dataview;

import org.catacomb.datalish.Box;
import org.catacomb.dataview.build.Displayable;
import org.catacomb.dataview.build.Line;
import org.catacomb.graph.gui.*;
import org.catacomb.numeric.data.DataExtractor;

import java.util.ArrayList;


public class DataPlotPainter implements PaintInstructor {


    DataExtractor dataExtractor;

    Displayable[] items;

    int currentFrame;

    Box limitBox;

    Labeller labeller;

    int widthFactor;


    public DataPlotPainter(ArrayList<Displayable> arl) {
        widthFactor = 1;

        if (arl == null) {
            items = new Displayable[0];

        } else {
            int n = arl.size();
            items = new Displayable[n];
            int iit = 0;
            for (Displayable dbl : arl) {
                items[iit++] = dbl;
            }
        }
    }


    public void setPaintWidthFactor(int ithick) {
        widthFactor = ithick;
    }


    public void markNeeded() {
        for (int i = 0; i < items.length; i++) {
            items[i].markNeeded(dataExtractor);
        }

    }



    public void setDataSource(DataExtractor dex) {
        currentFrame = -1;
        dataExtractor = dex;

        showFrame(0);
    }


    public void showFrame(int iframe) {
        if (iframe != currentFrame && dataExtractor != null) {
            currentFrame = iframe;

            for (int i = 0; i < items.length; i++) {
                items[i].getData(dataExtractor, currentFrame);
                if (items[i] instanceof Line) {
                }
            }
        }


        makeLims();

        if (labeller == null) {
            labeller = new Labeller(items.length);
            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof Line) {
                    Line ll = (Line)(items[i]);
                    Labellee lle = ll.getLabellee();
                    if (lle != null) {
                        labeller.updateLabellee(i, lle);
                    }
                }
            }
            labeller.initLabels(limitBox);
        } else {
            labeller.adjustLabels(limitBox);
        }


    }


    private void makeLims() {
        Box limits = new Box();
        for (int i = 0; i < items.length; i++) {
            items[i].pushBox(limits);
        }
        limits.pad();
        limitBox = limits;
    }


    public Box getLimitBox() {
        makeLims();
        return limitBox;
    }

    public boolean antialias() {
        return true;
    }



    public void instruct(Painter p) {
//      p.setLimitBox(limitBox);

        for (int i = 0; i < items.length; i++) {
            items[i].instruct(p, widthFactor);
        }

        if (labeller != null) {
            labeller.instruct(p);
        }
    }


}
