package org.catacomb.druid.build;


import org.catacomb.druid.market.HookupBoard;
import org.catacomb.interlish.content.ColorSet;
import org.catacomb.interlish.structure.Marketplace;
import org.catacomb.report.E;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;



public class Context {

    ColorSet colorSet;

    HookupBoard hookupBoard;
    GUIStore guiStore;

    static InfoAggregator infoAggregator;

    ArrayList<Object> cache;
    HashMap<String, ContingencyGroup> contingencyGroups;

    String sourceLocation;

    Context parentContext;


    public Context() {
        this(null);
    }


    public Context(Context pc) {
        parentContext = pc;

        if (parentContext != null) {
            colorSet = parentContext.copyColorSet();

        } else {
            colorSet = new ColorSet();
            colorSet.setGray();
        }

        guiStore = new GUIStore();
        guiStore.setInfoReceiver(getInfoAggregator());

    }


    // TODO any case for the aggregator not being static
    public InfoAggregator getInfoAggregator() {

        if (infoAggregator == null) {
            infoAggregator = new InfoAggregator();
            getMarketplace().global().addProducer("Info", infoAggregator, "info");
        }
        return infoAggregator;
    }




    public ContingencyGroup getContingencyGroup(String gnm) {
        if (contingencyGroups == null) {
            contingencyGroups = new HashMap<String, ContingencyGroup>();
        }

        ContingencyGroup ret = null;
        if (contingencyGroups.containsKey(gnm)) {
            ret = contingencyGroups.get(gnm);
        } else {
            ret = new ContingencyGroup(gnm);
            contingencyGroups.put(gnm, ret);
        }
        return ret;
    }



    public Marketplace getMarketplace() {
        if (hookupBoard == null) {

            // dont use from parent context, either local or use the global one;

            hookupBoard = new HookupBoard();

            guiStore.setHookupBoard(hookupBoard);
        }
        return hookupBoard;
    }

    public Marketplace getMarketplace(String scope) {
        Marketplace hub = getMarketplace();
        if (scope != null) {
            if (scope.equals("global")) {
                hub = hub.global();
            } else {
                E.warning("unrecognized scope value " + scope);
            }
        }
        return hub;
    }

    public ColorSet getColorSet() {
        return colorSet;
    }

    public ColorSet copyColorSet() {
        return colorSet.copy();
    }

    public Color getBg() {
        return colorSet.getBackground();
    }


    public Color getFg() {
        return colorSet.getForeground();
    }


    public void addToCache(Object obj) {
        if (cache == null) {
            cache = new ArrayList<Object>();
        }
        if (cache.contains(obj)) {
            E.info("cache already got - not adding " + obj);
            // OK;
        } else {
            cache.add(obj);
        }
    }


    public void addComponent(Object obj, GUIPath gpath) {
        guiStore.addComponent(obj, gpath);
    }


    public ArrayList<Object> getCache() {
        return cache;
    }


    /*
       public void setBackground(Color c) {
          colorSet.setBackground(c);

          Color bgColor = colorSet.getBackground();
          if (bgColor.getRed() == 240 && bgColor.getGreen() == 240) {
             E.info("got the 240 color...");
              // (new Exception()).printStackTrace();
           }
       }



       public void setForeground(Color c) {
          colorSet.setForeground(c);
       }

    */

    public void dumpIDs() {
        E.info(guiStore.getTextDump());
    }


    public GUIStore getGUIStore() {
        return guiStore;
    }


    public Context simpleCopy() {
        Context ctx = new Context();
        ctx.setColorSet(colorSet);
        ctx.setInfoAgregator(getInfoAggregator());
        return ctx;
    }


    public void setColorSet(ColorSet cs) {
        colorSet = cs;

        Color bgColor = colorSet.getBackground();
        if (bgColor.getRed() == 240 && bgColor.getGreen() == 240) {
            E.info("*** set the 240 color...");
            // (new Exception()).printStackTrace();
        }
    }


    public void setInfoAgregator(InfoAggregator ia) {
        infoAggregator = ia;
    }


    public void resetStore() {
        guiStore.reset();
    }


    public HashMap<String, Object> getIdentifiedComponents() {
        return guiStore.getIdentifiedComponentMap();
    }



    public ArrayList<Object> getAnonymousComponents() {
        return guiStore.getAnonymousComponents();
    }


    public void setBg(Color color) {
        colorSet.setBg(color);
    }


    public void setSourceLocation(String spkg) {
        sourceLocation = spkg;
    }


    public String getSourceLocation() {
        return sourceLocation;
    }



    /*
     * public TargetStore getTargetStore() { return guiStore.getTargetStore(); }
     *
     * public ArrayList getComponents() { return guiStore.getComponents(); }
     */
}
