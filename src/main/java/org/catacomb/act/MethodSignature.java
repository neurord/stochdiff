package org.catacomb.act;

import java.util.ArrayList;

import org.catacomb.report.E;

public class MethodSignature implements BlockSignature {

    int type;

    public final static int USER_SOURCE = 1;
    public final static int SYSTEM_SOURCE = 2;
    public final static int REQUIRED_USER_SOURCE = 3;
    public final static int SUPER_SOURCE = 4;


    int sourceType;

    String info;
    String returnType;
    String functionName;
    ArrayList<String[]> args;
    String body;
    MethodBody methodBody;

    boolean userAccessible;

    boolean superdefd;


    public MethodSignature(String fn) {
        this(fn, UNKNOWN);
    }


    public MethodSignature(String fn, int typ) {
        functionName = fn;
        type = typ;
        sourceType = USER_SOURCE;
        userAccessible = true;
        superdefd = false;
    }

    public void setSuperDefined() {
        superdefd = true;
    }

    public boolean superDefined() {
        return superdefd;
    }

    public void setUserSource() {
        sourceType = USER_SOURCE;
    }

    public void setSuperSource() {
        sourceType = SUPER_SOURCE;
    }


    public void setSystemSource() {
        sourceType = SYSTEM_SOURCE;
    }

    public void setRequiredUserSource() {
        sourceType = REQUIRED_USER_SOURCE;
    }

    public void setReturnType(String s) {
        returnType = s;
    }

    public void addArgument(String typ, String nm) {
        String[] sa = {typ, nm};
        if (args == null) {
            args = new ArrayList<String[]>();
        }
        args.add(sa);
    }


    public void setBody(String txt) {
        body = txt;
        setSystemSource();
    }


    public void setType(int ity) {
        type = ity;
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

    public String toJavaSource() {
        return toJavaSource("");
    }

    public String toAbstractJavaSource() {
        return toJavaSource(" abstract ");
    }






    public String toJavaSource(String qualifier) {
        StringBuffer sb = new StringBuffer();

        if (info != null) {
            if (info.length() > 60 || info.indexOf("\n") > 0) {
                sb.append("/*\n");
                sb.append("  " + info.replaceAll("\n", "\n  "));
                sb.append("\n*/\n");

            } else {
                sb.append("//");
                sb.append(info);
                sb.append("\n");
            }
        }
        sb.append("public ");
        sb.append(qualifier);
        if (returnType != null) {
            sb.append(returnType + " ");
        } else {
            sb.append("void ");
        }
        sb.append(functionName + "(");
        sb.append(writeArgs());

        sb.append(")");
        if (qualifier.indexOf("abstract") >= 0) {
            sb.append(";\n");
        } else {
            sb.append(" {\n");
            if (body != null) {
                sb.append(body);
                if (body.trim().endsWith(";")) {
                    // OK as is;
                } else {
                    sb.append(";");
                }
                sb.append("\n");

            } else if (methodBody != null) {
                sb.append(methodBody.write());
                sb.append("\n");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }








    public MethodStub getStub() {
        MethodStub ret = new MethodStub();

        if (info != null) {
            ret.setInfo(info);
        }
        ret.setVisibility("public");
        ret.setReturnType(returnType);
        ret.setMethodName(functionName);
        ret.setArgList(writeArgs());

        if (body != null || methodBody != null) {
            E.warning("stubs ignoring non-null body? " + body + " " + methodBody);
        }
        return ret;
    }











    private String writeArgs() {
        StringBuffer sb = new StringBuffer();
        if (args != null && args.size() > 0) {
            boolean follower = false;
            for (String[] sa : args) {
                if (follower) {
                    sb.append(", ");
                }
                follower = true;
                sb.append(sa[0] + " " + sa[1]);
            }

        }
        return sb.toString();
    }


    public void setUserHidden() {
        userAccessible = false;
    }

    public boolean emptyBody() {
        boolean ret = true;
        if (body != null && body.trim().length() > 0) {
            ret = false;
        } else if (methodBody != null) {
            ret = false;
        }
        return ret;
    }

    public boolean isUserAccessible() {
        return userAccessible;
    }

    public boolean isSuper() {
        return (sourceType == SUPER_SOURCE);
    }

    public boolean isSystem() {
        return (sourceType == SYSTEM_SOURCE);
    }

    public boolean isUser() {
        return (sourceType == USER_SOURCE);
    }

    public boolean isRequiredUser() {
        return (sourceType == REQUIRED_USER_SOURCE);
    }


    public Object writeCommentSignature() {
        StringBuffer sb = new StringBuffer();
        if (returnType == null || returnType.equals("void")) {
            sb.append("   ");
        } else {
            sb.append("  " + returnType + " v = ");
        }
        sb.append(functionName + "(" + writeArgs() + ")");
        return sb.toString();
    }


    public void addBodyCaseRelay(String svar, String sid, String call) {
        if (methodBody == null) {
            methodBody = new CaseRelayBody(svar);
        }
        ((CaseRelayBody)methodBody).addCase(sid, call);
    }


    public void appendToBody(String line) {
        setSystemSource();
        if (body == null) {
            body = line;
        } else {
            body = body + ";\n   " + line;
        }
    }


}
