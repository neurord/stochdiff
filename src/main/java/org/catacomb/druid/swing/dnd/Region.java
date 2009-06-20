package org.catacomb.druid.swing.dnd;

import org.catacomb.interlish.structure.TextDisplayed;


public class Region {


    public static final int DRAG = 1;

    public static final int DROP = 2;

    public static final int DRAG_OR_DROP = 3;

    public static final int CLICK = 0;




    int p_x;
    int p_y;
    int p_w;
    int p_h;

    String p_tag;
    Object p_ref;
    int p_action;


    @SuppressWarnings("unused")
    public Region(int x, int y, int w, int h, String string, Object object, int a) {
        p_x = x;
        p_y = y;
        p_w = w;
        p_h = h;
        p_action = a;
    }


    public Region(int[] xxyy, String s, Object o, int a) {
        p_x = xxyy[0];
        p_y = xxyy[2];
        p_w = xxyy[1] - xxyy[0];
        p_h = xxyy[3] - xxyy[2];

        p_tag = s;
        p_ref = o;
        p_action = a;
    }


    public String toString() {
        return ("regoin (" + p_tag + ", " + p_ref + ", " + p_action + ")");
    }


    public String getTag() {
        return p_tag;
    }

    public Object getRef() {
        return p_ref;
    }


    public boolean equals(Region r) {
        return (r.p_x == p_x && r.p_y == p_y && r.p_w == p_w && r.p_h == p_h);
    }


    public int getX() {
        return p_x;
    }

    public int getY() {
        return p_y;
    }

    public int getW() {
        return p_w;
    }

    public int getH() {
        return p_h;
    }


    public boolean contains(int x, int y) {
        return (x > p_x && x < (p_x + p_w) && y > p_y && y < (p_y + p_h));
    }


    public boolean acceptsDrops() {
        return (p_action == DROP || p_action == DRAG_OR_DROP);
    }


    public String getText() {
        String sret = "-missing-";
        if (p_ref instanceof TextDisplayed) {
            sret = ((TextDisplayed)p_ref).getDisplayText();
        } else if (p_ref != null) {
            sret = p_ref.toString();
        }
        return sret;
    }


    public static boolean canPress(int i) {
        return (i == DRAG || i == DRAG_OR_DROP || i == CLICK);
    }


    public boolean draggable() {
        return (p_action == DRAG || p_action == DRAG_OR_DROP);
    }

    public boolean clickable() {
        return (p_action == CLICK);
    }

}
