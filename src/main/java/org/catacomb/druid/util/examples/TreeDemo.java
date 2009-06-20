package org.catacomb.druid.util.examples;


import java.util.ArrayList;
import java.util.Collection;



public class TreeDemo {


    public static Collection getRelatedSet() {

        String[] names = {"chemistry", "physics", "organic", "inorganic",
                          "abstract", "physical", "qualifier", "item1",
                          "item2", "item3"
                         };

        String[] rels = {"is", "belongsin"};

        int[][] graph = {{},
            {},
            {1, 0},   // organic belongsin chemistry
            {1, 0},
            {},
            {},
            {0, 4},   //  qualifier is abstract
            {0, 6,  1, 3}, // item1 is quialifier belongsin inorganic
            {0, 5, 0, 4, 1, 1},
            {0, 4, 1, 3}
        };

        ArrayList<TreeItem> ret = new ArrayList<TreeItem>();

        int nit = graph.length;
        TreeItem[] items = new TreeItem[nit];

        for (int i = 0; i < nit; i++) {
            items[i] = new TreeItem(names[i]);
            ret.add(items[i]);
        }


        for (int i = 0; i < graph.length; i++) {
            int[] gd = graph[i];
            for (int k = 0; k < gd.length; k += 2) {
                items[i].addRelationship(items[gd[k+1]], rels[gd[k]]);
            }
        }

        return ret;
    }





}
