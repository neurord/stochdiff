package org.catacomb.druid.build;

import java.util.ArrayList;



public class Scope {




    public static ArrayList<String> getFullPaths(String scope) {
        String[] as = split(scope);
        String current = null;

        ArrayList<String> fp = new ArrayList<String>();

        for (String sit : as) {
            if (sit.startsWith(".")) {
                fp.add(current + sit);
            } else {
                fp.add(sit);

                int idot = sit.indexOf(".");
                if (idot > 0) {
                    current = sit.substring(0, idot);
                } else {
                    current = sit;
                }
            }
        }

        return fp;
    }



    private final static String[] split(String s) {
        String[] ret = null;
        if (s == null) {
            ret = new String[0];
        } else {
            ret = s.split("[;, ]");
        }
        return ret;
    }








    /*



    // way too long - do with a single regex?
    public final static String[] getFrameIDs(String scope) {
       String[] as = split(scope);

       int nf = 0;
       String[] wk = new String[as.length];

       for (int i = 0; i < as.length; i++) {
     String s = as[i];
     int idot = s.indexOf(".");
     if (idot < 0) {
        idot = s.length();
     }
     if (idot > 0) {
        String pref = as[i].substring(0, idot);

        if (!contains(wk, nf, pref)) {
           wk[nf++] = pref;
        }
     }
       }

       String[] ret = shrink(wk, nf);

       return ret;
    }



    public final static String[] getSubIDs(String fid, String scope) {
       if (fid == null || fid.equals("")) {
     return split(scope);

       } else {

     String[] as = split(scope);
     int ns = 0;
     String[] wk = new String[as.length];

     boolean inditto = false;

     for (int i = 0; i < as.length; i++) {
        String s = as[i];
        if (s.equals(fid)) {
           wk[ns++] = s;
           inditto = true;

        } else {
           int idot = s.indexOf(".");
           if (idot == 0 && inditto) {
    	  wk[ns++] = s.substring(1, s.length());

           } else if (idot > 0) {
    	  String sfid = s.substring(0, idot);
    	  if (sfid.equals(fid)) {
    	     inditto = true;
    	     wk[ns++] = s.substring(idot+1, s.length());
    	  } else {
    	     inditto = false;
    	  }
           }
        }
     }

     String[] ret = shrink(wk, ns);
     return ret;
       }
    }









    private final static String[] shrink(String[] sa, int n) {
       String[] ret = new String[n];
       for (int i = 0; i < n; i++) {
     ret[i] = sa[i];
       }
       return ret;

    }


    private final static boolean contains(String[] sa, int n, String s) {
       boolean ret = false;
       for (int k = 0; k < n; k++) {
     if (sa[k].equals(s)) {
        ret = true;
        break;
     }
       }
       return ret;
    }






    */







    /* unnecessary - never tested

    public final static String[][] getIDsByFrame(String scope) {
       String[] as = split(scope);
       int nset = 0;
       int lens = new int[as.length];
       String[] wk = new String[as.length][as.length+1];

       int iset = -1;

       for (int i = 0; i < as.length; i++) {
     String s = as[i];

     // fatten frame ids so they say eg MainFrame.MainFrame
     if (s.indexOf(".") < 0) {
        s = s + "." + s;
     }



     int idot = s.indexOf(".");

     if (idot > 0) {
        String pref = as[i].substring(0, idot);

        boolean got = false;
        for (int k = 0; k < nset; k++) {
           if (wk[k][0].equals(pref)) {
    	  got = true;
    	  iset = k;
           }
        }

        if (!got) {
           iset = nset;
           wk[iset][0] = pref;
           lens[iset] = 1;
           nset += 1;
        }
     }

     if (iset >= 0) {
        String rest = as[i].substring(idot+1, as[i].length);
        wk[iset][lens[iset]] = rest;
        lens[iset] += 1;

     } else {
        E.error(" - unresolved id reference " + s + " in " + scope);
     }
       }

       String[][] ret = new String[nset];



       for (int i = 0; i < nset; i++) {
     ret[i] = shrink(wk[i], lens[i]);
       }

       System.out.println("extracted ids from " + scope);
       dump(ret);


       return ret;
    }


    */




}
