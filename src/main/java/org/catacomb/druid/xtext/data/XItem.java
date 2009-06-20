package org.catacomb.druid.xtext.data;


import java.util.HashMap;

public class XItem {

    XType type;
    String val;

    HashMap<String, String> fields;

    public XItem(XType xt, String text) {
        type = xt;
        val = text;
        fields = new HashMap<String, String>();
    }

    public XType getType() {
        return type;
    }

    public String getText() {
        return val;
    }

    public String getFieldValue(String sid) {
        String ret = null;
        if (fields.containsKey(sid)) {
            ret = fields.get(sid);
        }
        return ret;
    }

    public void setField(String fid, String fval) {
        fields.put(fid, fval);
    }

}
