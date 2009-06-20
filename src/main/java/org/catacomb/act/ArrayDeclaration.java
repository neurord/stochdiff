package org.catacomb.act;

import java.util.ArrayList;

public class ArrayDeclaration {

    String type;
    String name;

    ArrayList<String> vals;

    public ArrayDeclaration(String sn) {
        name = sn;
        vals = new ArrayList<String>();
    }

    public void setType(String st) {
        type = st;
    }

    public void addValue(String s) {
        vals.add(s);
    }

    public String toJava() {
        StringBuffer sb = new StringBuffer();
        sb.append("public " + type + "[] " + name + " = {");

        boolean follower = false;
        for (String sv : vals) {
            if (follower) {
                sb.append(",\n               ");
            }
            sb.append(sv);
            follower = true;
        }

        sb.append("};\n");
        return sb.toString();
    }


}
