package org.catacomb.act;


public class EnumOption {

    String name;
    int value;

    public EnumOption(String nm, int val) {
        name = nm;
        value = val;
    }


    public String toJavaDeclaration() {
        return "  public final static int " + name + " = " + value + ";\n";
    }

    public String toPrefixedCodeComment(String pfx) {
        return pfx + "." + name + " ";
    }

}
