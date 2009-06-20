package org.catacomb.dataview.read;




public abstract class BaseContentReader implements ContentReader {


    FUImportContext importContext;


    public BaseContentReader(FUImportContext ctxt) {
        importContext = ctxt;
    }

    public abstract Object getMain();


    public FUImportContext getContext() {
        return importContext;
    }



}
