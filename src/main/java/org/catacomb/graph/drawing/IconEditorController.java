
package org.catacomb.graph.drawing;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.io.File;
import java.util.ArrayList;




public class IconEditorController implements Controller {


    ArrayList<Controller> connectors;




    public IconEditorController() {
        connectors = new ArrayList<Controller>();
// MISSING
    }



    @SuppressWarnings("unused")
    public void addChildController(Controller ctrl) {
        E.missing();
    }

    @SuppressWarnings("unused")
    public void show(Object obj) {
        // MISSING
    }


    public ArrayList<Controller> getConnectors() {
        return connectors;
    }
    public void attached() {
    }

    @SuppressWarnings("unused")
    public void loadFile(File f) {
        E.missing();
        /*
        Object obj = Importer.importFile(f);

        Table table = (Table)obj;

           show(table);
           */
        //      dmEditor.setDocument(table);
    }


    @SuppressWarnings("unused")
    public void saveToFile(File f) {
        //      Table table = dmEditor.getDocument();

        //      Archivist.storeXMLWithReferents(table, f);
    }



}
