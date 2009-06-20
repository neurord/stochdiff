package org.catacomb.act;


public class SuperCallConstructor {

    String argtype;
    String body = null;

    public SuperCallConstructor(String atyp) {
        argtype = atyp;
    }

    public SuperCallConstructor(String atyp, String bdy) {
        argtype = atyp;
        body = bdy;
    }

    public String toJavaSource(String cnm) {
        StringBuffer sb = new StringBuffer();
        if (argtype != null) {
            sb.append("   public " + cnm + "(" + argtype + " v) {\n");
            sb.append("      super(v);\n");
        } else {
            sb.append("   public " + cnm + "() {\n");
            sb.append("      super();\n");
        }
        if (body != null) {
            sb.append("      " + body + ";\n");
        }
        sb.append("   }\n");
        return sb.toString();
    }


}
