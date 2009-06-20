package org.catacomb.druid.util.tree;

import org.catacomb.interlish.structure.Related;
import org.catacomb.interlish.structure.Relationship;
import org.catacomb.interlish.structure.SingleParent;
import org.catacomb.report.E;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;



public class Trawler {


    public static Collection trawl(Related rel) {
        HashSet<Related> hset = new HashSet<Related>();

        addIfNew(hset, rel);

        return hset;
    }



    public static void addIfNew(HashSet<Related> hset, Related rel) {
        if (hset.contains(rel)) {
            // there already - nothing to do;

        } else {
            hset.add(rel);
            Relationship[] rels = rel.getRelationships();
            for (int i = 0; i < rels.length; i++) {
                addIfNew(hset, rels[i].getTarget());
            }
        }
    }




    public static ArrayList<Related> trawlChildren(SingleParent sp) {
        ArrayList<Related> arl = new ArrayList<Related>();
        addAll(arl, sp);
        return arl;
    }


    public static void addAll(ArrayList<Related> arl, SingleParent sp) {
        ArrayList<? extends Object> arc = sp.getChildren();

        arl.add(sp);

        for (Object obj : arc) {

            if (obj instanceof SingleParent) {
                addAll(arl, (SingleParent)obj);

            } else if (obj instanceof Related) {
                arl.add((Related)obj);

            } else {
                E.error(" - Relation Tree Trawlse found unrelated elt " +
                        obj + " " + obj.getClass());
            }
        }
    }



}
