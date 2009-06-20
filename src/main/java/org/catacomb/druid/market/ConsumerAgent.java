package org.catacomb.druid.market;

import org.catacomb.interlish.structure.Consumer;
import org.catacomb.interlish.structure.Dependent;
import org.catacomb.interlish.structure.OptionsUser;
import org.catacomb.report.E;



public class ConsumerAgent implements Dependent {

    Consumer consumer;
    String modality;
    String item;


    public ConsumerAgent(Consumer c, String mod, String sit) {
        consumer = c;
        modality = mod;
        item = sit;

    }


    public String getModality() {
        return modality;
    }

    public String getInterestedIn() {
        return item;
    }


    public void newValue(Object obj) {
        if (modality.equals("ChoiceOptions") &&
                (consumer instanceof OptionsUser) &&
                obj instanceof String[]) {

            ((OptionsUser)consumer).setOptions((String[])obj);


        } else {
            E.missing(" consumer=" + consumer + "\n  modality=" + modality + "\n item=" + item +
                      "\n value=" + obj);
        }
    }


}
