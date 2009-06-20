package org.catacomb.druid.market;

import org.catacomb.interlish.structure.*;
import org.catacomb.interlish.util.JUtil;

import java.util.HashMap;



public class HookupBoard implements Marketplace {

    static HookupBoard globalBoard;
    static int nextBoardNo = 0;

    String id;

    HashMap<String, ProducerConsumerBoard> boards;


    public HookupBoard() {
        boards = new HashMap<String, ProducerConsumerBoard>();
        nextBoardNo += 1;
        id = "HB_" + nextBoardNo;
    }

    public String toString() {
        return "Mkt " + id;
    }



    public Marketplace global() {
        if (globalBoard == null) {
            globalBoard = new HookupBoard();
//         E.info("made global board " + globalBoard);
        }
        return globalBoard;
    }


    public void addProducer(String modality, Producer p, String flavor) {
        getPCBoard(modality).addProducer(p, flavor);
    }


    public void addConsumer(String modality, Consumer c, String flavor) {
        getPCBoard(modality).addConsumer(c, flavor);
    }


    public void addProvider(String modality, Provider p, String flavor) {
        getPCBoard(modality).addProvider(p, flavor);
    }



    public void addReceiver(String modality, Receiver rec, String flavor) {
        getPCBoard(modality).addReceiver(rec, flavor);
    }


    // supplier will get you the flavor you ask for
    public void addSupplier(String modality, Supplier sup) {
        getPCBoard(modality).addSupplier(sup);
    }



    public void addVisible(String modality, Visible vbl, String flavor) {
        getPCBoard(modality).addVisible(vbl, flavor);
    }


    public void addViewer(String modality, Viewer vwr, String flavor) {
        getPCBoard(modality).addViewer(vwr, flavor);
    }



    private ProducerConsumerBoard getPCBoard(String modality) {
        ProducerConsumerBoard ret = null;

        if (boards.containsKey(modality)) {
            ret = (boards.get(modality));

        } else {
            ret = makePCBoard(modality);
            //      E.info("made board " + modality);
            boards.put(modality, ret);
        }
        return ret;
    }



    private ProducerConsumerBoard makePCBoard(String modality) {
        // TODO refac to direct class ref;
        String scnm = "org.catacomb.druid.market." + modality + "Board";
        Object ret = JUtil.newInstance(scnm);
        ProducerConsumerBoard pcb = (ProducerConsumerBoard)ret;
        pcb.setModality(modality);
        return pcb;
    }



    public void logUnresolved() {
        if (boards != null) {
            for (ProducerConsumerBoard pcb : boards.values()) {
                pcb.logUnresolved(id);
            }
        }
    }



}
