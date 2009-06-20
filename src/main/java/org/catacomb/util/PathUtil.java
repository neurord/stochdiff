package org.catacomb.util;



public class PathUtil {

    static String separator = ".";

    // convert a dot notation package name to a resource path with slashes;
    public static String dotsToSlashPath(String sin) {
        String s = sin;
        s = s.replaceAll("\\.", "/") + "/";
        return s;
    }


    public static String lastPathSegment(String pth) {
        int ils = pth.lastIndexOf(separator);
        String ret = pth.substring(ils+1, pth.length());
        return ret;
    }


    public static String parentPath(String pth) {
        int ils = pth.lastIndexOf(separator);
        String ret = null;
        if (ils > 0) {
            ret = pth.substring(0, ils);
        } else {
            ret = pth;
        }
        return ret;
    }


    public static String parentPackage(String pth) {
        return parentPath(pth);
    }




    public static String fileRoot(String fnm) {
        String ret = fnm;
        int ild = fnm.lastIndexOf(".");
        if (ild > 0) {
            ret = ret.substring(0, ild);
        }
        return ret;
    }


    public static String getSlashPackage(Object obj) {
        String fcn = obj.getClass().getName();
        int ild = fcn.lastIndexOf(".");
        if (ild <= 0) {
            fcn = "";
        } else {
            fcn = fcn.substring(0, ild);
        }
        String spkg = dotsToSlashPath(fcn);
        return spkg;
    }


    public static String shortName(final String ocnm) {
        String ret = ocnm.substring(ocnm.lastIndexOf(".") + 1, ocnm.length());
        return ret;
    }


    public static String dotPathify(String sin) {
        String s = sin;
        if (s.endsWith(".xml")) {
            s = s.substring(0, s.length() - 4);
        }
        s = s.replaceAll("/", ".");
        return s;
    }


    public static String[] getPackagePath(String sin) {
        String s = sin;
        String[] ret = new String[0];
        if (s.lastIndexOf("/") > 0) {
            s = s.substring(0, s.lastIndexOf("/"));
            ret  = s.split("/");
        }
        return ret;
    }

    public static String getPackage(String sin) {
        String s = sin;
        if (s.endsWith(".xml")) {
            s = s.substring(0, s.length() - 4);
        }
        String ret = "";
        if (s.lastIndexOf("/") > 0) {
            s = s.substring(0, s.lastIndexOf("/"));
            ret  = s.replaceAll("/", ".");

        } else if (s.lastIndexOf(".") > 0) {
            ret = s.substring(0, s.lastIndexOf("."));
        }
        return ret;
    }

}
