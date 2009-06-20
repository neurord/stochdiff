package org.catacomb.dataview.read;




public class CustomJarContentReader extends BaseContentReader {


    CustomJarReader customJarReader;

    JarImportContext ctxt;


    public CustomJarContentReader(byte[] bytes, FUImportContext ctxt) {
        super(ctxt);

        JarImportContext jctx = new JarImportContext(ctxt);

        customJarReader = new CustomJarReader(bytes, jctx);

        jctx.setJarReader(customJarReader);
    }



    public Object getMain() {
        return customJarReader.getMain();
    }



    public boolean hasRelative(String rp) {
        return customJarReader.hasRelative(rp);
    }


    public Object getRelative(String rp) {
        return customJarReader.getRelative(rp);
    }



}
