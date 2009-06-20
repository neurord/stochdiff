
package org.catacomb.numeric.data;



public class IntegerArray {

    int nitem;
    Integer[] items;


    public IntegerArray() {
        items = new Integer[3];
    }

    public void add(Integer iint) {

        if (nitem >= items.length) {
            Integer[] lia = new Integer[(3 * nitem) / 2];
            System.arraycopy(items, 0, lia, 0, nitem);
            items = lia;
        }

        items[nitem++] = iint;
    }


    public void clear() {
        nitem = 0;
    }


}
