package org.catacomb.druid.market;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;




public abstract class ProducerConsumerBoard {


    HashMap<String, ArrayList<Producer>> producers;
    HashMap<String, Consumer> consumers;
    HashMap<String, Receiver> receivers;
    HashMap<String, Provider> providers;

    HashMap<String, Visible> visibles;
    HashMap<String, ArrayList<Viewer>> viewers;

    ArrayList<Supplier> suppliers;


    String modality; // each board handles one modality;



    public ProducerConsumerBoard() {
        producers = new HashMap<String, ArrayList<Producer>>();
        consumers = new HashMap<String, Consumer>();
        receivers = new HashMap<String, Receiver>();
        providers = new HashMap<String, Provider>();
        suppliers = new ArrayList<Supplier>();
        visibles = new HashMap<String, Visible>();
        viewers = new HashMap<String, ArrayList<Viewer>>();
    }



    public void setModality(String s) {
        modality = s;
    }



    public void addProducer(Producer producer, String flavor) {

        /*
        if (modality.equals("Info")) {
           E.longInfo("addeda a producer of info " + producer);
        }
        */

        if (consumers.containsKey(flavor)) {
            connect(producer, consumers.get(flavor));
            consumers.remove(flavor);

            // E.info("removed consumer " + modality + " " + flavor);

        } else if (receivers.containsKey(flavor)) {
            connect(producer, receivers.get(flavor));

        } else {

            if (producers.containsKey(flavor)) {
                producers.get(flavor).add(producer);
                // E.info("added producer to existing list " + modality + " " +
                // flavor);

            } else {
                ArrayList<Producer> alp = new ArrayList<Producer>();
                alp.add(producer);
                producers.put(flavor, alp);
                // E.info("added producer to new list " + modality + " " + flavor);
            }


        }
    }



    public void addConsumer(Consumer consumer, String flavor) {
        // E.info("PCBoard add consumer " + modality + " " + flavor);


        if (producers.containsKey(flavor)) {

            ArrayList<Producer> alp = producers.get(flavor);
            if (alp.size() > 1) {
                E.error("muptiple producdrs of " + flavor + " in " + modality + ": need a receiver "
                        + "but got a consumer: " + consumer);
            }
            connect(alp.get(0), consumer);
            producers.remove(flavor);
            // E.info("removed producer " + modality + " " + flavor);

        } else if (providers.containsKey(flavor)) {
            connect(providers.get(flavor), consumer);

        } else {
            if (checkSuppliersFor(consumer, flavor)) {
                // OK;
                // E.info("got supplier " + modality + " " + flavor);

            } else {
                consumers.put(flavor, consumer);

                // E.info("added consumer " + modality + " " + flavor);
            }
        }
    }


    public void addReceiver(Receiver rec, String flavor) {
        // E.info("added receiver for " + flavor + " in " + modality);

        if (receivers.containsKey(flavor)) {
            E.error("multiple receivers of " + flavor + " only last one used " + rec);
        }
        receivers.put(flavor, rec);

        checkReceiverProducers(rec, flavor);
    }



    public void addSupplier(Supplier sup) {
        if (suppliers == null) {
            suppliers = new ArrayList<Supplier>();
        }
        suppliers.add(sup);
        checkConsumerSuppliers();

    }


    private void checkReceiverProducers(Receiver rec, String flav) {
        if (producers.containsKey(flav)) {

            for (Producer p : producers.get(flav)) {
                connect(p, rec);
            }

            producers.remove(flav);
        }
    }


    private void checkConsumerSuppliers() {
        if (consumers != null) {
            for (Map.Entry<String, Consumer> me : consumers.entrySet()) {
                checkSuppliersFor(me.getValue(), me.getKey());
            }
        }
    }



    private boolean checkSuppliersFor(Consumer c, String item) {
        boolean ret = false;

        if (suppliers != null) {
            for (Supplier sup : suppliers) {
                if (sup.canSupply(modality, item)) {

                    // E.info("found supplier for " + modality + " " + item);

                    sup.addDependent(new ConsumerAgent(c, modality, item));
                    ret = true;
                    break;
                }
            }
        }

        return ret;
    }

    public void connect(Producer producer, Consumer consumer) {
        E.override();
    }



    public void logUnresolved(String boardID) {
        logUnresolved(producers, "Producer", boardID);
        logUnresolved(consumers, "Consumer", boardID);
        logUnresolved(viewers, "Viewer", boardID);

    }

    private void logUnresolved(HashMap<?, ?> hm, String s, String boardID) {
        /* TODO reinstate log and report some other way
        if (hm != null) {
           for (Object key : hm.keySet()) {
              E.shortWarning("Mkt: " + boardID + " unresolved  " + s +
                       " (" + modality + "; " + key + ") " + hm.get(key));
           }
        }
        */
    }


    public void addVisible(Visible vbl, String flavor) {



        if (visibles.containsKey(flavor)) {
            E.warning("visible item being overridden: modality=" + modality +
                      " flavor=" + flavor + " item=" + visibles.get(flavor));
        }
        visibles.put(flavor, vbl);
        checkViewers(flavor, vbl);
    }



    public void addViewer(Viewer vwr, String flavor) {

        if (visibles.containsKey(flavor)) {
            connectVisibleViewer(visibles.get(flavor), vwr);

        } else {
            if (viewers.containsKey(flavor)) {
                viewers.get(flavor).add(vwr);
            } else {
                ArrayList<Viewer> arv = new ArrayList<Viewer>();
                arv.add(vwr);
                viewers.put(flavor, arv);
            }
        }

    }


    private void checkViewers(String flv, Visible vbl) {
        if (viewers.containsKey(flv)) {
            for (Viewer vwr : viewers.get(flv)) {
                connectVisibleViewer(vbl, vwr);
            }
            viewers.remove(flv);
        }
    }

    public void connectVisibleViewer(Visible vbl, Viewer vwr) {
        E.override();
    }



    public void addProvider(Provider provider, String flavor) {
        if (consumers.containsKey(flavor)) {
            connect(provider, consumers.get(flavor));
            consumers.remove(flavor);
        }

        if (providers.containsKey(flavor)) {
            E.warning("multiple providers of " + flavor + " " + modality);
        } else {
            providers.put(flavor, provider);
        }

    }

}
