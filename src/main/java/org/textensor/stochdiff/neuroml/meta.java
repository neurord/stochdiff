package org.textensor.stochdiff.neuroml;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.inter.XMLContainer;



public class meta implements AddableTo, XMLContainer {

    String content = "";

    public String tag;
    public String value;


    public ArrayList<MetaItem> items = new ArrayList<MetaItem>();

    public ArrayList<meta> metas = new ArrayList<meta>();

    public void add(Object obj) {
        if (obj instanceof MetaItem) {
            items.add((MetaItem)obj);
        } else if (obj instanceof meta) {
            metas.add((meta)obj);
        } else {
            E.error("cannot add " + obj);
        }
    }

    public MetaItem newItem(String nm) {
        MetaItem ret = new MetaItem(nm);
        items.add(ret);
        return ret;
    }


    public void setXMLContent(String s) {
        // TODO Auto-generated method stub

    }

    public void appendContent(String s) {
        content += s;
        content += " ";
    }


    public String getLabel() {
        return content;
    }


}
