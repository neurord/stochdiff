package org.catacomb.druid.xtext.base;

import org.catacomb.interlish.content.IntPosition;


public class TextFloat {

    String text;
    int x;
    int y;

    int width;
    int height;

    Object srcObject;


    public TextFloat(String s, int px, int py, Object src) {
        text = s;
        x = px;
        y = py;
        srcObject = src;
        width = -1;
        height = 16;
    }


    public TextFloat(String txt, IntPosition pos, Object src) {
        this(txt, pos.getX(), pos.getY(), src);
    }


    public String getText() {
        return text;
    }


    public Object getSource() {
        return srcObject;
    }

    public void setWidth(int w) {
        width = w;
    }

    public void setPosition(int px, int py) {
        x = px;
        y = py;
    }





    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    public void setPosition(IntPosition pos) {
        x = pos.getX();
        y = pos.getY();
    }
}
