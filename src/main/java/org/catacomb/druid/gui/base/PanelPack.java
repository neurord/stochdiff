package org.catacomb.druid.gui.base;


import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.util.ArrayList;
import java.util.HashMap;


public class PanelPack {

    DruPanel mainPanel;

    HashMap<String, Object> components;

    ArrayList<Object> anonCpts;


    public PanelPack(DruPanel dp, ArrayList<Object> anons, HashMap<String, Object> hm) {
        mainPanel = dp;
        anonCpts = anons;
        components = hm;
    }


    public DruPanel getMainPanel() {
        return mainPanel;
    }


    public boolean hasComponent(String sid) {
        return components.containsKey(sid);
    }


    public Object getComponent(String sid) {
        Object ret = null;
        if (components.containsKey(sid)) {
            ret = components.get(sid);
        } else {
            E.error("no such component in panel pack " + sid);
            for (String s : components.keySet()) {
                E.info("known cpt " + s);
            }
        }
        return ret;
    }


    public void setText(String cpt, String txt) {
        if (hasComponent(cpt)) {
            Object obj = getComponent(cpt);
            if (obj instanceof TextSettable) {
                ((TextSettable)obj).setText(txt);
            } else {
                E.error("cant set text in " + obj);
            }

        } else {
            E.error("no such cpt in panel pack " + cpt);
            reportComponents();
        }
    }




    public void actionConnect(Object tgt) {
        DruActionRelay dar = new DruActionRelay(tgt);
        for (Object obj : anonCpts) {
            connectRelay(dar, obj);
        }

        for (Object obj : components.values()) {
            connectRelay(dar, obj);
        }

    }


    private void connectRelay(DruActionRelay dar, Object obj) {
        if (obj instanceof ActionSource) {
            ((ActionSource)obj).setActionRelay(dar);
        }
    }


    public void reportComponents() {
        int icpt = 0;
        for (String s : components.keySet()) {
            E.info("known cpt " + icpt + " " + s);
            icpt += 1;
        }
    }


    public void setStringValue(String cpt, StringValue sv) {
        if (hasComponent(cpt)) {
            Object obj = getComponent(cpt);
            if (obj instanceof StringValueEditor) {
                ((StringValueEditor)obj).setStringValue(sv);
            } else {
                E.error("cant set text in " + obj);
            }

        } else {
            E.error("no such cpt in panel pack " + cpt);
            reportComponents();
        }

    }

}
