package org.catacomb.numeric.data;

import java.util.ArrayList;

import org.catacomb.interlish.structure.Parent;
import org.catacomb.report.E;


public class NDSAccessor {

    NumDataStore store;


    public NDSAccessor(NumDataStore ndstore) {
        store = ndstore;
    }


    public NumDataItem getNumDataItem(String s) {

        NumDataItem numv = null;

        ArrayList<Object> op = getObjectPath(s);

        if (op != null && op.size() > 0) {
            Object obj = op.get(op.size()-1);
            if (obj instanceof NumDataItem) {
                numv = (NumDataItem)obj;

            } else {
                E.warning("wrong type " + obj + " when seeking " + s +
                          " expecting NDI but got " +
                          obj.getClass().getName());
            }
        }
        return numv;
    }





    public ArrayList<Object> getObjectPath(String s) {
        ArrayList<Object> ret = new ArrayList<Object>();

        String[] sa = s.split("/");

        Object osf = store;
        ret.add(osf);
        int ioff = 0;
        if (sa[0].equals(store.toString())) {
            ioff = 1;
        }

        for (int i = ioff; i < sa.length; i++) {
            if (osf instanceof Parent) {
                Parent par = (Parent)osf;
                if (par.hasChild(sa[i])) {
                    osf = par.getChild(sa[i]);
                    ret.add(osf);

                } else {
                    E.possibleError(" no child \"" + sa[i] + "\" in parent " + "\"" + par + "\"");
                    //     store.dumpChildren();
                    ret = null;
                    par = null;
                    break;
                }
            } else {
                E.error("need a parent object, not " + osf);
            }
        }

        return ret;
    }



    public ArrayList<NumDataItem> getOrdinates(NumVector numV) {
        ArrayList<NumDataItem> ret = new ArrayList<NumDataItem>();


        if (numV instanceof DSlice) {
            // just return the other slices? - too adhoc?
            BlockStack bs = (BlockStack)(((DSlice)numV).getParent());

            addStackSlices(bs, ret, numV);

        } else if (numV instanceof DVector) {
            int np = ((DVector)numV).getNPoint();

            for (DVector dv : store.getDVectors()) {
                if (dv.getNPoint() == np) {
                    ret.add(dv);
                }
            }

            for (BlockStack bs : store.getBlockStacks()) {
                if (bs.getSize() == np) {
                    addStackSlices(bs, ret, null);

                } else {
                    if (np == 1 || bs.getSize() == 1) {
                        // probably fine - don't report;
                    } else {
                        E.info("not adding a possible ordinate since it is a different size " +
                               "(" + np + " compared with " + bs.getSize() + ") " + bs.getName());
                    }
                }
            }

        } else {
            E.missing();
        }
        return ret;
    }


    private void addStackSlices(BlockStack bs, ArrayList<NumDataItem> ret,
                                Object excl) {
        for (Object obj : bs.getSlices()) {
            if (obj == excl) {
                // just leave it out;

            } else if (obj instanceof NumVector) {
                ret.add((NumDataItem)obj);

            } else if (obj instanceof NumVectorSet) {
                ret.add((NumDataItem)obj);

            } else if (obj instanceof AnimSlice) {
                // fine to ignore it;

            } else {
                E.info("ignoring " + obj);
            }
        }

    }


    public NumDataItem getSibling(NumVector numV, String snm) {
        NumDataItem ret = null;
        ArrayList<NumDataItem> ndis = getOrdinates(numV);
        for (NumDataItem ndi : ndis) {
            if (snm.equals(ndi.getName())) {
                ret = ndi;
                break;
            }
        }

        return ret;
    }


}
