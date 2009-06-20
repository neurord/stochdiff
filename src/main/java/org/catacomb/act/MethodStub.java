package org.catacomb.act;


public class MethodStub {

    public String info;
    public String returnType;
    public String methodName;
    public String visibility;

    public String arglist;


    public MethodStub() {

    }


    public void setInfo(String s) {
        if (s != null) {
            info = s;
        } else {
            info = "";
        }
    }

    public void setVisibility(String s) {
        if (s != null) {
            visibility = s;
        } else {
            visibility = "public";
        }
    }


    public void setReturnType(String s) {
        if (s != null) {
            returnType = s;
        } else {
            returnType = "void";
        }

    }

    public void setMethodName(String s) {
        methodName = s;
    }

    public void setArgList(String s) {
        arglist = s;
    }


    public String getMethodID() {
        // POSERR ignoring args
        String ret = returnType + ":" + methodName;
        return ret;
    }


}
