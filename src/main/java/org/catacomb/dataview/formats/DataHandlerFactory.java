package org.catacomb.dataview.formats;


public class DataHandlerFactory {


    /*
     * can make this fancier - plugin loader etc etc, but for now...
     */


    public static DataHandler getHandler(String s) {
        DataHandler ret = null;
        if (s.startsWith("cctswc00")) {
            ret = new SWCDisplay();

        } else if (s.startsWith("cctdif2d")) {
            ret = new Mesh2plusTimeDisplay();
        }
        return ret;
    }


}
