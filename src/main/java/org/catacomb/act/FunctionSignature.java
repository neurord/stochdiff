package org.catacomb.act;

import org.catacomb.report.E;


public class FunctionSignature implements BlockSignature {

    public int type;

    public int sourceType;

    public String info;
    public String functionName;
    public Object[] argTypes;


    public FunctionSignature(String fn) {
        this(fn, UNKNOWN);
    }


    public FunctionSignature(String fn, int typ) {
        functionName = fn;
        type = typ;
        sourceType = USER_SOURCE;
    }

    public void setUserSource() {
        sourceType = USER_SOURCE;
    }

    public void setSystemSource() {
        sourceType = SYSTEM_SOURCE;
    }


    public int getTypeCode() {
        return type;
    }


    public static String getTypeInfo(int itc) {
        String ret = "";
        if (itc == RECEIVE) {
            ret = "Handlers: functions that are called when an event occurs in a connected component.";

        } else if (itc == SEND) {
            ret = "Senders: these send an event to any connected components. The handler \n" +
                  "on the receiving component will be called.";


        } else if (itc == SETTER) {
            ret = "Setters: these set a value for use later, but have no other effect: \n" +
                  "the value is available to connected components if they ask for it";

        } else if (itc == GETTER) {
            ret = "Getters: give access to quantities in connected components.";
        }
        return ret;
    }


    public void setInfo(String s) {
        info = s;
    }


    public String getName() {
        return functionName;
    }


    public String toJavaScriptStub() {
        String ret = null;
        if (sourceType == SYSTEM_SOURCE) {
            ret = writeJsDoc();

        } else {
            ret = writeEmptyJsFunction();

        }
        return ret;
    }


    private String writeJsDoc() {
        StringBuffer sb = new StringBuffer();

        if (info != null) {
            if (info.indexOf("\n") > 0) {
                sb.append("/* Pre-defined function \n " + info + "\n*/\n");
            } else {
                sb.append("// Pre-defined function: " + info + "\n");
            }
        } else {
            sb.append("// Pre-defined  \n");
        }
        sb.append("//  ");
        sb.append(functionName);
        sb.append("(");
        if (argTypes != null) {
            E.missing();
        }
        sb.append(");\n");
        return sb.toString();
    }


    private String writeEmptyJsFunction() {
        StringBuffer sb = new StringBuffer();

        if (info != null) {
            if (info.length() > 60 || info.indexOf("\n") > 0) {
                sb.append("/*\n");
                sb.append(info);
                sb.append("\n");

            } else {
                sb.append("//");
                sb.append(info);
                sb.append("\n");
            }
        }
        sb.append("function ");
        sb.append(functionName);
        sb.append("(");
        if (argTypes != null) {
            E.missing();
        }
        sb.append(") {\n");
        sb.append("}\n");
        return sb.toString();
    }

}
