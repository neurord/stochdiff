package org.catacomb.util;


import org.catacomb.interlish.structure.IDd;

import java.util.ArrayList;


public class DocdUtils {



    public static String[] getIDArray(ArrayList<? extends IDd> elts) {
        return getIDArrayList(elts).toArray(new String[0]);
    }


    public static ArrayList<String> getIDArrayList(ArrayList<? extends IDd> elts) {

        ArrayList<String> sal = new ArrayList<String>();

        if (elts != null && elts.size() > 0) {
            for (IDd idd : elts) {
                sal.add(idd.getID());
            }
        }
        return sal;
    }



}
