package org.catacomb.druid.gui.base;



import org.catacomb.druid.gui.edit.Effect;
import org.catacomb.report.E;



public class DruCardSelectionEffect extends Effect  {

    String cardToShow;

    public DruCardSelectionEffect(String tgt, String s) {
        super(tgt);
        cardToShow = s;
    }




    public void apply(boolean b) {
        if (b) {


            //  E.info("card selection effect applying " + b + " " + cardToShow);

            Object tgt = getTarget();

            if (tgt instanceof DruPseudoCardPanel) {
                ((DruPseudoCardPanel)tgt).showCard(cardToShow);

            } else if (tgt instanceof DruCardPanel) {
                ((DruCardPanel)tgt).showCard(cardToShow);

            } else {
                E.error("must have card panelt, not " + tgt);
            }
        }
    }

}
