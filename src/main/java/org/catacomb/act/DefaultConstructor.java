package org.catacomb.act;


public class DefaultConstructor {

    String body;

    public DefaultConstructor() {

    }

    public DefaultConstructor(String bdy) {
        body = bdy;
    }

    public String toJavaSource(String cnm) {
        StringBuffer sb = new StringBuffer();
        sb.append("   public " + cnm + "() {\n");
        if (body != null) {
            sb.append("      " + body + ";\n");
        }
        sb.append("   }\n");
        return sb.toString();
    }


}
