package org.catacomb.act;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.catacomb.report.E;

public class Classdef {

    String info;
    String packageName;
    String className;
    String subclassName;
    String extendsName;

    String qualifier;

    SuperCallConstructor constructor;
    SuperCallConstructor subConstructor;

    DefaultConstructor econstructor;
    DefaultConstructor esubConstructor;


    ArrayList<FieldSignature> fields;
    HashMap<String, ArrayDeclaration> arrayDecs;

    ArrayList<MethodSignature> methods;

    public ArrayList<String> imports;
    ArrayList<String> implementsNames;

    String subclassImports;

    public Classdef() {
        qualifier = "";
        imports = new ArrayList<String>();
        extendsName = null;
        implementsNames = new ArrayList<String>();
        fields = new ArrayList<FieldSignature>();
        arrayDecs = new HashMap<String, ArrayDeclaration>();
        methods = new ArrayList<MethodSignature>();
    }

    public void setPackage(String s) {
        packageName = s;
    }

    public void setQualifier(String s) {
        qualifier = s;
    }

    public void addImplements(String s) {
        addImplementsName(s);
    }

    public void setClassName(String s) {
        className = s;
    }

    public String getClassName() {
        return className;
    }

    public void setSubclassName(String s) {
        subclassName = s;
    }

    public void setSuperclassName(String s) {
        extendsName = s;
    }

    public void addImplementsName(String s) {
        implementsNames.add(s);
    }

    public void addField(FieldSignature ms) {
        fields.add(ms);
    }

    public void addMethod(MethodSignature ms) {
        methods.add(ms);
    }

    public MethodSignature getMethod(String mnm) {
        MethodSignature ret = null;
        for (MethodSignature ms : methods) {
            if (ms.getName().equals(mnm)) {
                ret = ms;
                break;
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public void addImport(Object obj) {
        if (obj instanceof Class) {
            addStringImport(((Class)obj).getName());
        } else {
            addStringImport(obj.getClass().getName());
        }
    }


    public void addStringImport(String s) {
        if (imports.contains(s)) {

        } else {
            imports.add(s);
        }
    }


    private String makePackageStatement() {
        return "package " + packageName + ";\n";
    }


    private String makeJavaImports() {
        StringBuffer sb = new StringBuffer();
        for (String s : imports) {
            if (s.equals(packageName + "." + className) ||
                    s.equals(packageName + ".*")) {
                // not needed;

            } else {
                sb.append("import ");
                sb.append(s);
                sb.append(";\n");
            }
        }
        return sb.toString();
    }

    public void setInfo(String msg) {
        info = msg;
    }


    public MethodSignature newMethod(String mnm) {
        MethodSignature ms = new MethodSignature(mnm);
        addMethod(ms);
        return ms;
    }

    public MethodSignature newShowMethod(String rtype, String mnm, String atype) {
        MethodSignature ms = new MethodSignature(mnm, MethodSignature.SHOW);
        ms.setReturnType(rtype);
        if (atype != null) {
            ms.addArgument(atype, "v");
        }
        addMethod(ms);
        return ms;
    }

    @SuppressWarnings("unused")
    public MethodSignature newReadMethod(String rtype, String mnm, String atype) {
        MethodSignature ms = new MethodSignature(mnm, MethodSignature.READ);
        ms.setReturnType(rtype);
        addMethod(ms);
        return ms;
    }



    public MethodSignature newSendMethod(String mnm, String atype) {
        MethodSignature ms = new MethodSignature(mnm, MethodSignature.SEND);
        ms.setReturnType("void");
        if (atype != null) {
            ms.addArgument(atype, "v");
        }
        addMethod(ms);
        return ms;
    }


    public MethodSignature newReceiveMethod(String mnm, String atype) {
        MethodSignature ms = new MethodSignature(mnm, MethodSignature.RECEIVE);
        ms.setReturnType("void");
        if (atype != null) {
            ms.addArgument(atype, "v");
        }
        addMethod(ms);
        return ms;
    }


    public FieldSignature newPrivateField(String typ, String nm) {
        FieldSignature fsig = new FieldSignature(typ, nm);
        fsig.setPrivate();
        addField(fsig);
        return fsig;
    }


    public FieldSignature newPublicField(String typ, String nm) {
        FieldSignature fsig = new FieldSignature(typ, nm);
        fsig.setPublic();
        addField(fsig);
        return fsig;
    }

    public FieldSignature newReflectableField(String typ, String nm) {
        FieldSignature fsig = new FieldSignature(typ, nm);
        fsig.setReflectable();
        addField(fsig);
        return fsig;
    }

    public FieldSignature newGetSetField(String typ, String nm) {
        FieldSignature fsig = new FieldSignature(typ, nm);
        fsig.addGetter();
        fsig.addSetter();
        addField(fsig);
        return fsig;
    }


    public String writeSystemSource() {
        StringBuffer sb = new StringBuffer();

        sb.append(makePackageStatement());
        sb.append("\n");

        sb.append(makeJavaImports());
        sb.append("\n");
        sb.append("//IMPORTS\n");


        if (info != null) {
            sb.append("/*\n" + info + "\n*/\n");
        }
        sb.append("public " + qualifier + " class " + className + " ");
        if (extendsName != null) {
            sb.append("extends " + extendsName + " ");
        }
        if (implementsNames != null && implementsNames.size() > 0) {
            sb.append("implements ");
            boolean follower = false;
            for (String s : implementsNames) {
                if (follower) {
                    sb.append(",");
                }
                follower = true;
                sb.append(" " + s);
            }

        }
        sb.append(" {\n\n");

        for (FieldSignature fsig : fields) {
            sb.append(fsig.getJavaSource());
        }

        for (ArrayDeclaration ad : arrayDecs.values()) {
            sb.append(ad.toJava());
        }


        sb.append("\n\n");


        if (constructor != null) {
            sb.append(constructor.toJavaSource(className));
            sb.append("\n\n");
        }
        if (econstructor != null) {
            sb.append(econstructor.toJavaSource(className));
            sb.append("\n\n");
        }


        Collections.sort(methods, new SignatureComparator());

        for (FieldSignature fsig : fields) {
            sb.append(fsig.getJavaAccessors());
        }

        for (MethodSignature ms : methods) {
            String qf = "";
            if (ms.isRequiredUser()) {
                qf = " abstract ";
            }
            if (ms.isSuper()) {
                // these dont get code generated - just there for the info
                // of what is available in the superclass

            } else {
                if (ms.superDefined() && ms.emptyBody()) {
                    // should leave it out - may be overridden by CLASSBODY insertion;

                } else {
                    sb.append(ms.toJavaSource(qf));
                    sb.append("\n\n");
                }
            }
        }

        sb.append("//LINE " + nlines(sb) + " start of user content\n");
        sb.append("//CLASSBODY\n");
        sb.append("}\n");
        return sb.toString();
    }





    public String writeSubSource() {
        StringBuffer sb = new StringBuffer();
        sb.append(makePackageStatement());
        sb.append("\n");
        sb.append(makeJavaImports());
        sb.append("\n");
        if (subclassImports != null) {
            sb.append(subclassImports);
            sb.append("\n");
        }

        sb.append("//IMPORTS\n");

        if (info != null) {
            sb.append("/*\n" + info + "\n*/\n");
        }
        sb.append("public class " + subclassName + " ");
        sb.append("extends " + className + " ");
        sb.append(" {\n\n");


        if (subConstructor != null) {
            sb.append(subConstructor.toJavaSource(subclassName));
            sb.append("\n\n");
        }

        if (esubConstructor != null) {
            sb.append(esubConstructor.toJavaSource(subclassName));
            sb.append("\n\n");
        }
        sb.append("//CLASSBODY\n");

        sb.append("\n\n");

        sb.append("}\n");
        return sb.toString();
    }

    public String writeSystemFieldComments() {
        return writeSystemFieldComments("p");
    }

    public String writeSystemFieldComments(String pname) {
        StringBuffer sb = new StringBuffer();
        int npf = 0;

        if (fields != null) {
            for (FieldSignature fsig : fields) {
                if (fsig.isPublic() && fsig.isVisible()) {
                    sb.append("");
                    sb.append(fsig.getPrefixedInfo(pname));
                    sb.append("<br>\n");
                    npf += 1;
                }
            }
        }
        return sb.toString();
    }



    public int countAccessibleFields() {
        int nf = 0;
        for (FieldSignature fsig : fields) {
            if (fsig.isPublic() && fsig.isVisible()) {
                nf += 1;
            }
        }
        return nf;
    }



    public String writeLocalFieldComments() {
        StringBuffer sb = new StringBuffer();
        for (FieldSignature fsig : fields) {
            if (fsig.isPublic() && fsig.isVisible()) {
                sb.append("");
                sb.append(fsig.getLocalInfo());
                sb.append("<br>\n");
            }
        }
        if (sb.length() == 0) {
            sb.append(" - no local fields - ");
        }
        return sb.toString();
    }




    public String writeSystemSignatures() {
        StringBuffer sb = new StringBuffer();

        for (MethodSignature msig : methods) {
            if (msig.isSuper() || (msig.isSystem() && msig.isUserAccessible())) {
                sb.append("");
                sb.append(msig.writeCommentSignature());
                sb.append("<br>\n");
            }
        }
        if (sb.length() == 0) {
            sb.append(" - no access methods - <br>");
        }
        sb.append("\n");

        return sb.toString();
    }



    public String writeSubSourceMethods() {
        StringBuffer sb = new StringBuffer();

        for (MethodSignature msig : methods) {
            if (msig.isUser() || msig.isRequiredUser()) {
                sb.append(msig.toJavaSource());
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }


    public ScriptStubs getSubSourceMethodStubs() {
        ScriptStubs ret = new ScriptStubs();
        for (MethodSignature msig : methods) {
            if (msig.isUser() || msig.isRequiredUser()) {
                ret.add(msig.getStub());
            }
        }
        return ret;
    }


    public void addSuperCallConstructor() {
        addSuperCallConstructor(null);
    }

    public void addSuperCallConstructor(String argtype) {
        constructor = new SuperCallConstructor(argtype);
        subConstructor = new SuperCallConstructor(argtype);
    }

    public void addSuperCallConstructor(String argtype, String body) {
        constructor = new SuperCallConstructor(argtype, body);
        subConstructor = new SuperCallConstructor(argtype);
    }

    public void addDefaultConstructor(String sbody) {
        econstructor = new DefaultConstructor(sbody);
    }

    public void addDefaultConstructor() {
        econstructor = new DefaultConstructor();
        esubConstructor = new DefaultConstructor();
    }



    public void startArrayDeclaration(String sType, String sName) {
        ArrayDeclaration arrDec = new ArrayDeclaration(sName);
        arrDec.setType(sType);
        arrayDecs.put(sName, arrDec);

    }

    public void addToArray(String anm, String aval) {
        if (arrayDecs.containsKey(anm)) {
            arrayDecs.get(anm).addValue(aval);
        } else {
            E.warning("no sucn array declaration : " + anm);
        }
    }


    public FieldSignature addGetSetField(String st, String sn) {
        FieldSignature fsig = new FieldSignature(st, sn);
        fsig.addGetter();
        fsig.addSetter();
        fsig.setPublic();
        addField(fsig);
        return fsig;
    }

    public void setUserHidden(String methname) {
        getMethod(methname).setUserHidden();
    }

    public void appendToBody(String methname, String line) {
        MethodSignature msig = getMethod(methname);
        if (msig == null) {
            msig = new MethodSignature(methname);
            addMethod(msig);
        }

        msig.appendToBody(line);
    }

    public void addSubclassImports(String s) {
        if (subclassImports == null) {
            subclassImports = s;
        } else {
            subclassImports += "\n" + s;
        }

    }


    private int nlines(StringBuffer sb) {
        String s = sb.toString();
        int nocc = 0;
        int ioff = s.indexOf("\n");
        while (ioff >= 0) {
            nocc++;
            ioff = s.indexOf("\n", ioff+1);
        }
        return nocc;
    }

}
