package org.catacomb.numeric.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.catacomb.datalish.RunDataBlock;
import org.catacomb.datalish.SpriteStore;
import org.catacomb.interlish.content.BasicTouchTime;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;



public class BlockStack implements Named, TreeNode, Parent {

    ArrayList<RunDataBlock> items;

    String name;

    ArrayList<StackSlice> slices;
    HashMap<String, StackSlice> sliceHM;


    BasicTouchTime changeTime;

    boolean doneInit;

    TreeChangeReporter tcReporter;

    NumDataStore store;


    public BlockStack(NumDataStore p, String snm) {
        name = snm;
        store = p;
        doneInit = false;
        items = new ArrayList<RunDataBlock>();

        slices = new ArrayList<StackSlice>();
        sliceHM = new HashMap<String, StackSlice>();
        changeTime = new BasicTouchTime();

    }


    public String getName() {
        return name;
    }


    public String toString() {
        return name;
    }


    public Object getParent() {
        return store;
    }


    public BasicTouchTime getChangeTime() {
        return changeTime;
    }


    public void addToStack(RunDataBlock str) {
        if (!doneInit) {
            initSlices(str);
            doneInit = true;
        }
        items.add(str);
        changeTime.now();
        reportValueChange();
    }



    public void clear() {
        items.clear();
        for (StackSlice ss : sliceHM.values()) {
            ss.clear();
        }
        changeTime.now();
    }




    private void initSlices(RunDataBlock proto) {
        HashMap<String, Object> staticsHM = new HashMap<String, Object>();



        for (Field f : proto.getClass().getFields()) {
            String fnm = f.getName();

            String unit = "";
            String title = f.getName();

            String[] arrayEltNames = null;

            // just keep the string arrays and drawings;
            if ((f.getType().isArray() && f.getType().getComponentType().equals(String.class))
                    || (f.getType().equals(XYVectorSprite.class))
                    || (f.getType().equals(XYVectorScene.class))) {
                try {
                    Object obj = f.get(proto);
                    if (obj != null) {
                        staticsHM.put(f.getName(), obj);
                    }
                } catch (Exception ex) {

                }

            } else if (f.isAnnotationPresent(Quantity.class)) {
                Quantity q = f.getAnnotation(Quantity.class);
                unit = q.unit();
                title = q.title();

            } else if (f.isAnnotationPresent(QuantityDA.class)) {
                QuantityDA qda = f.getAnnotation(QuantityDA.class);
                unit = qda.unit();
                title = qda.title();
                String nameSet = qda.nameSet();

                if (nameSet != null && nameSet.length() > 0) {
                    if (staticsHM.containsKey(qda.nameSet())) {
                        arrayEltNames = (String[])(staticsHM.get(nameSet));
                    } else {
                        E.warning("ref to names set " + nameSet + " - not found");
                    }
                }

            } else if (f.isAnnotationPresent(SpriteState.class)) {
                SpriteState spr = f.getAnnotation(SpriteState.class);
                title = spr.title();


            } else if (f.isAnnotationPresent(MultiSprites.class)) {
                MultiSprites spr = f.getAnnotation(MultiSprites.class);
                title = spr.title();


            } else {
                E.warning("all fields in blocks should be annotated with a Quantity " + " annotation: "
                          + f.getName() + " in " + proto);
                // leave as is;

            }


            if (f.isAnnotationPresent(MultiSprites.class)) {
                MultiSprites spr = f.getAnnotation(MultiSprites.class);
                title = spr.title();
                SpriteStore ss = store.getSpriteStore();

                AnimSlice ans = new AnimSlice(this, fnm, f, title, ss);
                sliceHM.put(fnm, ans);
                slices.add(ans);
                // POSERR - could look sprites up now?


            } else if (f.getType().equals(Double.TYPE)) {
                DSlice sds = new DSlice(this, fnm, f, unit, title);
                sliceHM.put(fnm, sds);
                slices.add(sds);

            } else if (f.getType().isArray()) {
                Class<?> ctyp = f.getType().getComponentType();

                if (ctyp.equals(String.class)) {
                    // SKIP - presumably it was a name set? - check;

                } else if (ctyp.equals(Double.TYPE)) {
                    DDSlice sdas = new DDSlice(this, fnm, f, unit, title, arrayEltNames);
                    sliceHM.put(fnm, sdas);
                    slices.add(sdas);

                } else if (ctyp.equals(Integer.TYPE)) {
                    E.shortWarning(" missing code for int[] arrays");

                } else if (ctyp.isArray()) {
                    E.shortWarning("missing code for array of arrays");


                } else {
                    E.error("cannot handle array of type " + ctyp);
                }

            } else if (f.getType().equals(XYVectorScene.class)) {
                // already handled;

            } else {
                E.error("unrecognized class " + f + " " + f.getType());
            }

        }
        reportNewFields();
    }


    public int getSize() {
        return items.size();
    }


    public RunDataBlock getBlock(int i) {
        return items.get(i);
    }


    private void reportNewFields() {
        // E.info("BlockStack about to report new fields " + getChildCount());
        tcReporter.nodeAddedUnder(this, null);
    }


    private void reportValueChange() {

    }


    public void setTreeChangeReporter(TreeChangeReporter tcr) {
        tcReporter = tcr;
    }



    public int getChildCount() {
        return slices.size();
    }


    public Object getChild(int index) {
        return slices.get(index);
    }


    public int getIndexOfChild(Object child) {
        return slices.indexOf(child);
    }


    public boolean isLeaf() {
        return false;
    }


    public boolean hasChild(String s) {
        return sliceHM.containsKey(s);
    }


    public Object getChild(String s) {
        return sliceHM.get(s);
    }


    public ArrayList<StackSlice> getSlices() {
        return slices;
    }


    public NumVector getFirstArraySlice() {
        NumVector ret = null;
        for (StackSlice ss : slices) {
            if (ss instanceof NumVector) {
                ret = (NumVector)ss;
                break;
            }

        }
        return ret;
    }


    public Collection<? extends AnimSlice> getMovies() {
        ArrayList<AnimSlice> msls = new ArrayList<AnimSlice>();
        for (StackSlice sl : slices) {
            if (sl instanceof AnimSlice) {
                msls.add((AnimSlice)sl);
            }
        }
        return msls;
    }

}
