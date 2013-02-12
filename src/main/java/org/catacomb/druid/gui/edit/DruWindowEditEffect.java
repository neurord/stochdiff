package org.catacomb.druid.gui.edit;

import org.catacomb.report.E;





public class DruWindowEditEffect extends Effect {


    String config;

    public DruWindowEditEffect(String targetCpt, String cfg) {
        super(targetCpt);
        config = cfg;
    }




    public void apply(boolean b) {
        Object tgt = getTarget();
        E.missing("cannot access edot ctrl " + tgt);
//      Sys.getSys().getModelEditor().showEditor(config, tgt);
    }


}
