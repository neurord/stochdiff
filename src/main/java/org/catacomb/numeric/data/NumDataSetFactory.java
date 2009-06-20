package org.catacomb.numeric.data;

import org.catacomb.interlish.structure.Attribute;
import org.catacomb.interlish.structure.Element;
import org.catacomb.interlish.structure.Factory;
import org.catacomb.report.E;




public class NumDataSetFactory implements Factory {


    public String makes;




    public Object make(String s) {
        NumDataSet dset = new NumDataSet(s);

        return dset;
    }


    private NumDataSet makeDataSet(Element elt) {
        NumDataSet ds = new NumDataSet(elt.getName());
        populate(ds, elt);
        return ds;
    }



    public void populate(Object obj, Element popelt) {
        NumDataSet ds = (NumDataSet)obj;
        populate(ds, popelt);
    }


    private void populate(NumDataSet ds, Element popelt) {
        Attribute[] atta = popelt.getAttributeArray();
        for (int i = 0; i < atta.length; i++) {
            Attribute att = atta[i];
            if (att.getName().equals("name")) {
                ds.setName(att.getValue());
            } else {
                ds.addVectorOrScalar(att.getName(), att.getValue());
            }
        }


        Element[] elta = popelt.getElementArray();
        for (int i = 0; i < elta.length; i++) {
            Element elt = elta[i];

            if (elt.getName().equals("VectorSet")) {
                VectorSet vset = makeVectorSet(elt);
                ds.addVectorSet(vset);

            } else if (elt.hasElements() || elt.hasAttributes()) {
                NumDataSet cds = makeDataSet(elt);
                ds.addDataSet(cds);

            } else {
                ds.addVectorOrScalar(elt.getName(), elt.getText());
            }
        }
    }



    private VectorSet makeVectorSet(Element popelt) {
        VectorSet vset = new VectorSet();
        vset.setNames(popelt.getAttribute("names"));
        Element[] elta = popelt.getElementArray();
        for (int i = 0; i < elta.length; i++) {
            Element elt = elta[i];
            if (elt.getName().equals("row")) {
                vset.addRow(new FloatRow(elt.getText()));

            } else {
                E.error("only row elements allowed in a vector set, not " + elt.getName());
            }
        }
        return vset;
    }


}
