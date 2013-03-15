package org.catacomb.numeric.data;

import org.catacomb.datalish.RunDataBlock;
import org.catacomb.datalish.RunDataStore;
import org.catacomb.datalish.SpriteData;
import org.catacomb.datalish.SpriteStore;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.util.HashMap;
import java.util.ArrayList;

public class NumDataStore implements RunDataStore, Named, TreeRoot,
    TreeChangeReporter, Parent {

    String name;


    ArrayList<Object> children;

    HashMap<String, BlockStack> stackHM;

    HashMap<String, DVector> vecHM;
    HashMap<String, DVector[]> vecArrayHM;

    ArrayList<DVector> dVectors;
    ArrayList<BlockStack> blockStacks;

    ArrayList<EventSequence> evtSequences;
    HashMap <String, EventSequence> evtSeqHM;


    TreeChangeReporter tcReporter;

    ArrayList<DataWatcher> dataWatchers;

    NDSAccessor ndsAccessor;


    SpriteStore spriteStore;


    public double time;


    int nrep = 0;

    public NumDataStore(String s) {
        name = s;
        stackHM = new HashMap<String, BlockStack>();
        vecHM = new HashMap<String, DVector>();
        vecArrayHM = new HashMap<String, DVector[]>();

        dVectors = new ArrayList<DVector>();
        blockStacks = new ArrayList<BlockStack>();

        evtSequences = new ArrayList<EventSequence>();
        evtSeqHM = new HashMap<String, EventSequence>();

        children = new ArrayList<Object>();
        dataWatchers = new ArrayList<DataWatcher>();

        spriteStore = new SpriteStore();

        ndsAccessor = new NDSAccessor(this);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public void setTime(double t) {
        time = t;
    }

    public double getTime() {
        return time;
    }

    public SpriteStore getSpriteStore() {
        return spriteStore;
    }

    public ArrayList<DVector> getDVectors() {
        return dVectors;
    }

    public ArrayList<BlockStack> getBlockStacks() {
        return blockStacks;
    }

    public ArrayList<EventSequence> getEventSequences() {
        return evtSequences;
    }


    // REFAC - not best here
    public ArrayList<NumVector> getPossibleAbscissae() {
        ArrayList<NumVector> andi = new ArrayList<NumVector>();
        for (Object ch : children) {
            if (ch instanceof BlockStack) {
                NumVector numv = ((BlockStack)ch).getFirstArraySlice();
                if (numv != null) {
                    andi.add(numv);
                }
            }
        }
        if (dVectors != null && dVectors.size() > 0) {
            andi.add(dVectors.get(0));
        }
        return andi;
    }



    public void add(NumDataItem ndi) {
        if (ndi instanceof EventSequence) {
            evtSequences.add((EventSequence)ndi);
            children.add(ndi);
            evtSeqHM.put(ndi.getName(), (EventSequence)ndi);
            reportDataStructureChange();
            reportNewChild();
        } else {
            E.error("cannot add " + ndi);
        }

    }

    public void addSprite(SpriteData sd) {
        spriteStore.add(sd);
    }



    public ArrayList<AnimSlice> getMovies() {
        ArrayList<AnimSlice> msls = new ArrayList<AnimSlice>();
        for (Object ch : children) {
            if (ch instanceof BlockStack) {
                msls.addAll(((BlockStack)ch).getMovies());
            }
        }
        return msls;
    }



    public void addDataWatcher(DataWatcher dw) {
        // don't change the array list - it may be reporting a change
        ArrayList<DataWatcher> newdw = new ArrayList<DataWatcher>();
        newdw.addAll(dataWatchers);
        newdw.add(dw);
        dataWatchers = newdw;
    }



    public void removeDataWatcher(DataWatcher dw) {
        ArrayList<DataWatcher> newdw = new ArrayList<DataWatcher>();
        for (DataWatcher odw : dataWatchers) {
            if (odw == dw) {
                // leave it out;
            } else {
                newdw.add(odw);
            }
        }
        dataWatchers = newdw;
    }



    public void stack(RunDataBlock se) {
        String s = se.getClass().getName();
        s = s.substring(s.lastIndexOf(".")+1, s.length());
        stack(s, se);
    }



    public void addEvent(String snm, double t, int ich) {
        if (evtSeqHM.containsKey(snm)) {
            evtSeqHM.get(snm).addEvent(t, ich);
        } else {
            EventSequence es = new EventSequence(snm);
            evtSeqHM.put(snm, es);
            evtSequences.add(es);
            es.addEvent(t, ich);
            reportDataStructureChange();
        }
//      reportDataValueChange();   // EFF - too costly?
    }




    public void stack(String snm, double val) {
        if (vecHM.containsKey(snm)) {
            vecHM.get(snm).add(val);

        } else {
            DVector dvec = new DVector(this, snm);
            dVectors.add(dvec);
            dvec.add(val);
            vecHM.put(snm, dvec);
            children.add(dvec);
            reportNewChild();
            reportDataStructureChange();
        }
        reportDataValueChange();   // EFF - too costly?
    }


    public void stack(String snm, double[] val) {
        stack(snm, null, val);
    }


    public void stack(String snm, String[] labsin, double[] val) {
        String[] labs = labsin;
        DVector[] dva = null;
        if (vecArrayHM.containsKey(snm)) {
            dva = vecArrayHM.get(snm);
        } else {
            if (labs == null) {
                labs = new String[val.length];
                for (int i = 0; i < val.length; i++) {
                    labs[i] = "v_" + i;
                }
            }
            dva = new DVector[labs.length];
            vecArrayHM.put(snm, dva);
            for (int i = 0; i < labs.length; i++) {
                dva[i] = new DVector(this, labs[i]);
                children.add(dva[i]);
                vecHM.put(labs[i], dva[i]);
                dVectors.add(dva[i]);
            }

            E.info("stacked array " + labs.length);
            E.dump(labs);
            reportNewChild();
            reportDataStructureChange();
        }

        for (int i = 0; i < dva.length; i++) {
            dva[i].add(val[i]);
        }
        reportDataValueChange(); // EFF as above
    }




    public void stack(String snm, RunDataBlock str) {
        if (str instanceof Timestampable) {
            ((Timestampable)str).stampTime(time);
        }

        if (stackHM.containsKey(snm)) {
            stackHM.get(snm).addToStack(str);

        } else {
            BlockStack ss = new BlockStack(this, snm);
            stackHM.put(snm, ss);
            blockStacks.add(ss);

            children.add(ss);

            ss.setTreeChangeReporter(this);

            ss.addToStack(str);

            reportNewChild();
            reportDataStructureChange();
        }

        reportDataValueChange();

    }


    private void reportNewChild() {
        if (tcReporter != null) {
            tcReporter.nodeAddedUnder(this, null);
        } else {
            //   E.error("need a tree change reporter");
        }

    }


    public NumDataItem getNumDataItem(String s) {
        return ndsAccessor.getNumDataItem(s);
    }

    public ArrayList<NumDataItem> getOrdinates(NumVector numV) {
        return ndsAccessor.getOrdinates(numV);
    }

    public NumDataItem getSibling(NumVector numV, String snm) {
        return ndsAccessor.getSibling(numV, snm);
    }

    public void reportDataValueChange() {
        for (DataWatcher dw : dataWatchers) {
            dw.dataValueChanged(this, null);
        }
    }

    public void reportDataStructureChange() {
        for (DataWatcher dw : dataWatchers) {
            dw.dataStructureChanged(this, null);
        }
    }

    public void reportCompletion() {
        for (DataWatcher dw : dataWatchers) {
            dw.dataComplete(this);
        }
    }



    // for reporter interface;
    public void nodeAddedUnder(TreeNode parent, TreeNode child) {
        if (tcReporter != null) {
            tcReporter.nodeAddedUnder(parent, child);
        } else {
            E.error("need tcReporter");
        }
    }


    public void nodeRemoved(TreeNode parent, TreeNode child) {
        if (tcReporter != null) {
            tcReporter.nodeRemoved(parent, child);
        }
    }




    public Object getParent() {
        return null;
    }

    public TreeNode getRoot() {
        return this;
    }

    public int getRootPolicy() {
        return Tree.AUTO_ROOT;
    }

    public void setTreeChangeReporter(TreeChangeReporter tcr) {
        tcReporter = tcr;
    }

    public int getChildCount() {
        return children.size();
    }

    public Object getChild(int index) {
        return children.get(index);
    }

    public int getIndexOfChild(Object child) {
        return children.indexOf(child);
    }

    public boolean isLeaf() {
        return false;
    }




    public boolean hasChild(String s) {
        return (stackHM.containsKey(s) ||
                vecHM.containsKey(s) ||
                evtSeqHM.containsKey(s));
    }

    public Object getChild(String s) {
        Object ret = null;
        if (stackHM.containsKey(s)) {
            ret = stackHM.get(s);
        } else if (vecHM.containsKey(s)) {
            ret = vecHM.get(s);

        } else if (evtSeqHM.containsKey(s)) {
            ret = evtSeqHM.get(s);
        }
        return ret;
    }

    public Object[] getObjectPath(String s, boolean b) {
        return (ndsAccessor.getObjectPath(s)).toArray(new Object[0]);
    }





    public void dumpChildren() {
        for (Object obj : children) {
            E.info("store child: " + obj);
        }
    }


    // REFAC - must be used elsewhere too.
    public String getPath(Object obj) {
        String ret = null;
        if (obj instanceof Named) {
            ret = ((Named)obj).getName();
        }

        if (obj instanceof TreeNode) {
            Object par = ((TreeNode)obj).getParent();
            if (par == null) {

            } else {
                String pp = getPath(par);
                if (pp != null) {
                    ret = pp + "/" + ret;
                }
            }
        } else {
// OK- root of tree?        E.warning("should be a tree node? " + obj.getClass().getName());
        }
        return ret;
    }

    public void spriteAt(String snm, double t, double[] pxy, double[] hxy,
                         double[] wx, double[] wy) {
        stack(snm, new SimpleSpriteBlock(t, pxy, hxy, wx, wy));
    }


}
