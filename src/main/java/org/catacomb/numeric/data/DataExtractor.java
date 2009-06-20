package org.catacomb.numeric.data;


import java.util.HashMap;

import org.catacomb.interlish.reflect.Narrower;



/*
things I want to be able to write;

ds.time                                  - a scalar
ds.model[*].number                      - vector across models
ds.model.number                      - vector across models - allow this?
ds.model[20].age(log10)                 - log 10 of age of model 20
ds.luminosity
ds.model[20].luminosity[3]

ds.model[number=20].luminosity          - vector for a single model

*/




public class DataExtractor {

    HashMap<String, NumDataSet> sources;


    public DataExtractor() {
        sources = new HashMap<String, NumDataSet>();
    }


    public NumDataSet getDataSet(String s) {
        return (sources.get(s));
    }



    public void addDataSet(NumDataSet dset) {
        sources.put(dset.getName(), dset);
    }




    public double getScalar(String path) {
        DataSlice ds = get(path);
        return ds.getScalar();
    }


    public double[] getVector(String path) {
        double[] ret = null;
        if (path.startsWith("{")) {
            ret = getExplicitVector(path);
        } else {
            DataSlice ds = get(path);
            ret = ds.getVector();
        }
        return ret;
    }


    public int[] getIntVector(String path) {
        DataSlice ds = get(path);
        int[] ret = ds.getIntVector();
        return ret;
    }



    public void mark(String path) {
        DataSlice dsret = constructSlice(path);
        dsret.mark(sources);

    }



    private DataSlice get(String path) {
        DataSlice dsret = constructSlice(path);
        dsret.resolve(sources);

        return dsret;
    }



    private DataSlice constructSlice(String path) {

        String[] bits = path.split("\\.");
        DataSlice dsret = new DataSlice(bits[0]);

        DataSlice dscur = dsret;
        for (int i = 1; i < bits.length; i++) {
            DataSlice dssub = new DataSlice(bits[i]);
            dscur.setSubslice(dssub);
            dscur = dssub;
        }
        return dsret;
    }



    public double[] getExplicitVector(String stxt) {
        double[] ret = Narrower.readDoubleArray(stxt);
        return ret;
    }

}


