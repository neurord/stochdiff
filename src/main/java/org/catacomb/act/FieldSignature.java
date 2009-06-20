package org.catacomb.act;

import java.util.ArrayList;

public class FieldSignature {

    public final static int PRIVATE = 1;
    public final static int PUBLIC = 2;
    public final static int REFLECT_PUBLIC = 3;
    // access by reflection but not
    // documented as public - REFAC - clean up;
    public int type;

    String preComment;
    ArrayList<EnumOption> enumOptions;


    String vtype;
    String name;

    boolean getter;
    boolean setter;

    boolean visible = false;

    public FieldSignature(String typ, String nm) {
        vtype = typ;
        name = nm;
        type = PRIVATE;
        getter = false;
        setter = false;
    }


    public void addGetter() {
        getter = true;
    }

    public void addSetter() {
        setter = true;
    }

    public void setPrivate() {
        type = PRIVATE;
    }

    public void setPublic() {
        type = PUBLIC;
    }

    public void setVisible() {
        visible = true;
    }


    public void setReflectable() {
        // for fields that have to be public for reflection, but shouldn't be
        // otherwise accessible
        type = REFLECT_PUBLIC;
    }

    public String getJavaSource() {
        StringBuffer sb = new StringBuffer();

        if (preComment != null) {
            sb.append("// " + preComment + "\n");
        }
        if (enumOptions != null) {
            for (EnumOption eno : enumOptions) {
                sb.append(eno.toJavaDeclaration());
            }
        }


        sb.append("   ");
        if (type == PRIVATE) {
            sb.append("private ");
        } else if (type == PUBLIC || type == REFLECT_PUBLIC) {
            sb.append("public ");
        }
        sb.append(vtype);
        sb.append(" ");
        sb.append(name);
        sb.append(";\n");

        return sb.toString();
    }



    public void addEnumOption(String nm, int val) {
        if (enumOptions == null) {
            enumOptions = new ArrayList<EnumOption>();
        }
        enumOptions.add(new EnumOption(nm, val));
    }


    public String getPrefixedInfo(String pfx) {
        StringBuffer sb = new StringBuffer();
        String ufnm = getUName();
        if (getter) {
            sb.append("   " +  vtype + " " + pfx + ".get" + ufnm + "() ");

            if (enumOptions != null && enumOptions.size() > 0) {
                sb.append("\n      possible values: ");
                int ict = 0;
                for (EnumOption eno : enumOptions) {
                    ict++;
                    if (ict % 4 == 0) {
                        sb.append("\n             ");
                    }
                    sb.append(eno.toPrefixedCodeComment(pfx));
                }
            }
        } else {
            sb.append("   " + vtype + "  " + pfx + "." + name);
        }

        return sb.toString();
    }


    public String getLocalInfo() {
        return "     " + vtype + " " + name;
    }




    private String getUName() {
        String ufnm = name;
        if (name.length() == 1) {
            ufnm = name.toUpperCase();
        } else {
            ufnm = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
        }
        return ufnm;
    }



    public String getJavaAccessors() {
        StringBuffer sb = new StringBuffer();

        String ufnm = getUName();
        if (setter) {
            sb.append("   public void set" + ufnm + "(" + vtype + " v) {\n");
            sb.append("      this." + name + " = v;\n");
            sb.append("   }\n\n");
        }

        if (getter) {
            sb.append("   public " + vtype + " get" + ufnm + "() {\n");
            sb.append("      return " + name + ";\n");
            sb.append("   }\n\n");
        }
        return sb.toString();
    }


    public void addPreComment(String str) {
        preComment = str;

    }


    public boolean isPublic() {
        boolean ret = false;
        if (type == PUBLIC) {
            ret = true;
        }
        return ret;
    }

    public boolean isVisible() {
        return visible;
    }


}
