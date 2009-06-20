package org.catacomb.numeric.data;

import org.catacomb.report.E;

import java.util.HashMap;


public class DataSlice {


    String item = null;

    String func = null;
    String slice = null;


    DataSlice subslice;
    DataSlice[] subsliceArray;

    double scalarValue;
    double[] vectorValue;
    NumDataSet objectValue;
    DataSetArray arrayValue;

    int resultType;
    final static int SCALAR = 1;
    final static int VECTOR = 2;
    final static int OBJECT = 3;
    final static int ARRAY = 4;



    public DataSlice(String pe) {
        if (pe != null) {
            extractFrom(pe);
        }
    }



    private DataSlice copy() {
        DataSlice ret = new DataSlice(null);
        ret.item = item;
        ret.func = func;
        ret.slice = slice;
        if (subslice != null) {
            ret.subslice = subslice.copy();
        }
        return ret;
    }



    private void extractFrom(String pein) {
        String pe = pein;
        int iosb = pe.indexOf("[");
        int icsb = pe.lastIndexOf("]");
        if (iosb > 0 && icsb > iosb) {
            slice = pe.substring(iosb + 1, icsb);
            String npe = pe.substring(0, iosb);
            if (pe.length() > icsb) {
                npe += pe.substring(icsb + 1, pe.length());
            }
            pe = npe;
        }


        int iorb = pe.indexOf("(");
        int icrb = pe.lastIndexOf(")");
        if (iorb > 0 && icrb > iorb) {
            func = pe.substring(iorb + 1, icrb);
            pe = pe.substring(0, iorb);
        }
        item = pe;

        /*
         * E.info("parsed data slice " + peorig + " to " + " slice=" + slice + "
         * func=" + func + " item=" + item);
         */
    }


    public void setSubslice(DataSlice dsl) {
        subslice = dsl;
    }



    public void resolve(HashMap<String, NumDataSet> hmap) {
        NumDataSet target = hmap.get(item);
        if (target == null) {
            E.error("data set (type " + resultType + ") hash map has no element " + item);
            for (String s : hmap.keySet()) {
                E.info("hmap val " + hmap.get(s));
            }
        } else {
            applyResolve(target);
        }
    }



    public void mark(HashMap<String, NumDataSet> hm) {
        NumDataSet dset = hm.get(item);
        applyMark(dset);

    }


    public void mark(NumDataSet dset) {
        dset.mark();
        DataItem target = dset.get(item);
        if (target == null) {
            E.error("data slice result is null on  slice=" + slice + " func=" + func + " item=" + item
                    + " dataSet=" + dset);
        } else {
            applyMark(target);
        }
    }


    private void applyMark(DataItem ditin) {
        DataItem dit = ditin;
        dit.mark();

        if (dit instanceof DataSetArray) {
            if (slice != null && !(slice.equals("-1"))) {
                dit = ((DataSetArray)dit).slice(slice);
            }
            if (subslice != null) {

                NumDataSet[] dsa = ((DataSetArray)dit).getDataSets();

                for (int i = 0; i < dsa.length; i++) {
                    dsa[i].mark();
                    subslice.mark(dsa[i]);
                }
            }
        } else if (dit instanceof NumDataSet && subslice != null) {
            subslice.mark((NumDataSet)dit);
        }
    }



    public void resolve(NumDataSet dset) {
        Object target = dset.get(item);

        if (target == null) {
            E.error("data slice result is null on  slice=" + slice + " func=" + func + " item=" + item
                    + " dataSet=" + dset);
        } else {
            applyResolve(target);
        }
    }


    private void applyResolve(Object target) {

        if (target instanceof FloatScalar) {
            scalarValue = ((FloatScalar)target).getValue();
            if (func != null) {
                scalarValue = applyFuncToScalar(scalarValue, func);
            }
            resultType = SCALAR;


        } else if (target instanceof FloatVector) {
            vectorValue = ((FloatVector)target).getValue();
            if (slice != null) {
                vectorValue = applySliceToVector(vectorValue, slice);
            }
            if (func != null) {
                vectorValue = applyFuncToVector(vectorValue, func);
            }
            resultType = VECTOR;


        } else if (target instanceof NumDataSet) {
            objectValue = (NumDataSet)target;
            resultType = OBJECT;


        } else if (target instanceof DataSetArray) {

            arrayValue = ((DataSetArray)target);
            resultType = ARRAY;

            if (slice != null) {
                arrayValue = arrayValue.slice(slice);
                if (arrayValue.length() == 1) {
                    resultType = OBJECT;
                    objectValue = arrayValue.firstElement();
                }
            }

        } else {
            E.error("Data Slice - unknown target type " + target);
        }

        if (subslice != null) {
            if (resultType == OBJECT) {
                subslice.resolve(objectValue);

            } else if (resultType == ARRAY) {
                NumDataSet[] dsa = arrayValue.getDataSets();

                subsliceArray = new DataSlice[dsa.length];
                for (int i = 0; i < dsa.length; i++) {

                    subsliceArray[i] = subslice.copy();
                    subsliceArray[i].resolve(dsa[i]);
                }
                subslice = null;
            }
        }

    }



    public double getScalar() {
        double ret = 0.;
        if (subslice != null) {
            ret = subslice.getScalar();

        } else {
            if (resultType == SCALAR) {
                ret = scalarValue;

            } else if (resultType == VECTOR && vectorValue.length == 1) {
                ret = vectorValue[0];

            } else {
                E.error("requested scalar from non-scalar data slice " + resultType);
            }
        }
        return ret;
    }


    public double[] getVector() {
        double[] ret = null;

        if (subslice != null) {
            ret = subslice.getVector();

        } else if (subsliceArray != null) {
            int ns = subsliceArray.length;
            ret = new double[ns];
            for (int i = 0; i < ns; i++) {
                ret[i] = subsliceArray[i].getScalar();
            }

        } else if (resultType == VECTOR) {
            ret = vectorValue;

        } else if (resultType == OBJECT && subslice != null) {
            ret = subslice.getVector();

        } else {
            E.debugError("requested vector from non-scalar data slice " + resultType + " " + item);
        }
        return ret;
    }



    public int[] getIntVector() {
        int[] ret = null;

        if (subslice != null) {
            ret = subslice.getIntVector();

        } else if (subsliceArray != null) {
            int ns = subsliceArray.length;
            // REFAC
            ret = new int[ns];
            for (int i = 0; i < ns; i++) {
                ret[i] = i;
            }

        } else if (resultType == ARRAY) {
            int ns = arrayValue.length();
            ret = new int[ns];
            for (int i = 0; i < ns; i++) {
                ret[i] = i;
            }

        } else {
            E.error("cant get int vector from " + this + " restype=" + resultType);
        }
        return ret;
    }



    public void markVector() {

    }


    public void markIntVector() {

    }


    public void markScalar() {

    }



    // generalize REFAC !
    private double[] applyFuncToVector(double[] da, String lfunc) {
        double[] ret = new double[da.length];
        if (lfunc.equals("log10")) {
            double mlten = Math.log(10.);
            for (int i = 0; i < da.length; i++) {
                ret[i] = Math.log(da[i]) / mlten;
            }
        } else {
            E.error("Data slice unknown function " + lfunc);
        }
        return ret;
    }



    private double applyFuncToScalar(double d, String lfunc) {
        double ret = 0.;
        if (lfunc.equals("log10")) {
            double mlten = Math.log(10.);
            ret = Math.log(d) / mlten;
        } else {
            E.error("Data slice unknown function " + lfunc);
        }
        return ret;
    }



    // REFAC generalize;
    private double[] applySliceToVector(double[] da, String lslice) {
        double[] ret = new double[1];
        int ival = Integer.parseInt(lslice);
        ret[0] = da[ival];
        return ret;
    }



}
