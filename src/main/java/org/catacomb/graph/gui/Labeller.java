package org.catacomb.graph.gui;

import org.catacomb.datalish.Box;



public class Labeller {


    int nlabellee;
    Labellee[] labellees;



    public Labeller(int nle) {
        nlabellee = nle;
        labellees = new Labellee[nlabellee];
    }

    public void updateLabellee(int ile, Labellee lle) {
        labellees[ile] = lle;
    }




    public void initLabels(Box b) {
        double xmin = b.getXmin();
        double xmax = b.getXmax();
        double ymin = b.getYmin();
        double ymax = b.getYmax();

        int io = 0;
        double xl = xmin + (0.1 * xmax - xmin);

        for (int i = 0; i < nlabellee; i++) {
            if (labellees[i] != null) {
                double yl = ymax + (io+2) * (ymin - ymax)/ 10.;
                io++;
                labellees[i].setLabelPosition(xl, yl);

            }
        }

    }



    @SuppressWarnings("unused")
    public void adjustLabels(Box b) {

    }


    public void instruct(Painter p) {
        for (int i = 0; i < labellees.length; i++) {
            if (labellees[i] != null) {
                labellees[i].instruct(p);
            }
        }
    }


}
