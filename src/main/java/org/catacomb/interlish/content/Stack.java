package org.catacomb.interlish.content;


import java.util.ArrayList;

public class Stack<V> {

    ArrayList<V> items;

    public Stack() {
        items = new ArrayList<V>();
    }


    public void push(V elt) {
        items.add(0, elt);
    }

    public V pop() {
        V ret = null;
        if (items.size() > 0) {
            ret = items.get(0);
            items.remove(0);
        }
        return ret;
    }

}
