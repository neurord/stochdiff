package org.catacomb.druid.build;

import org.catacomb.druid.market.HookupBoard;
import org.catacomb.interlish.structure.InfoReceiver;
import org.catacomb.interlish.structure.Marketplace;
import org.catacomb.interlish.structure.TargetStore;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GUIStore {

    DruidTargetStore targetStore;
    HashMap<String, ArrayList<Object>> withoutIDs;
    ArrayList<Object> allComponents;

    InfoReceiver infoReceiver;


    public GUIStore() {
        reset();
    }


    public void reset() {
        targetStore = new DruidTargetStore();
        withoutIDs = new HashMap<String, ArrayList<Object>>();
        allComponents = new ArrayList<Object>();
        //   E.info("reset store ");
    }






    public void addComponent(Object obj, GUIPath gpath) {
        if (gpath.isUnique()) {
            addIDdComponent(obj, gpath.getPath());

        } else {
            addAnonymousComponent(obj, gpath.getPath());
        }

        allComponents.add(obj);
    }


    public HashMap<String, ArrayList<Object>> getAnonymousComponentMap() {
        return withoutIDs;
    }


    public HashMap<String, Object> getIdentifiedComponentMap() {
        return targetStore.getIdentifiedComponentMap();
    }


    public void addIDdComponent(Object obj, String id) {

        targetStore.addComponent(id, obj);
    }



    public void addAnonymousComponent(Object obj, String path) {
        if (withoutIDs.containsKey(path)) {
            (withoutIDs.get(path)).add(obj);

        } else {
            ArrayList<Object> arl = new ArrayList<Object>();
            arl.add(obj);
            withoutIDs.put(path, arl);
        }

        /*
         * E.info("GEN stored " + path); E.info(" " + obj);
         */
    }



    public String getTextDump() {
        StringBuffer sb = new StringBuffer();

        sb.append("\n uniquely identified components\n");
        appendHM(sb, targetStore.getHashMap());

        sb.append("\n\n");
        sb.append("generic container components\n");
        appendHM(sb, withoutIDs);
        sb.append("\n\n");
        return sb.toString();
    }


    private void appendHM(StringBuffer sb, HashMap<String, ? extends Object> hm) {
        for (String s : hm.keySet()) {
            sb.append("   ");
            sb.append(s);
            sb.append("\n");
        }
    }



    public TargetStore getTargetStore() {
        return targetStore;
    }


    public ArrayList getComponents() {
        return allComponents;
    }


    public void setHookupBoard(HookupBoard hb) {
        targetStore.setHookupBoard(hb);
    }


    public HookupBoard getHookupBoard() {
        return targetStore.getHoookupBoard();
    }


    public void setInfoReceiver(InfoReceiver ir) {
        infoReceiver = ir;
        targetStore.setInfoReceiver(ir);

    }

    public InfoReceiver getInfoReceiver() {
        return infoReceiver;
    }


    public void clear() {
        targetStore.clear();
    }

    public HashMap<String, Object> getIdentifiedComponents() {
        return targetStore.getHashMap();
    }


    public ArrayList<Object> getAnonymousComponents() {
        ArrayList<Object> ret = new ArrayList<Object>();

        for (Map.Entry<String, ArrayList<Object>> entry : withoutIDs.entrySet()) {
            ret.addAll(entry.getValue());
        }
        return ret;
    }


    public Marketplace getMarketplace() {
        return getHookupBoard();
    }


}
