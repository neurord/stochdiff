package org.catacomb.druid.xtext.base;



import org.catacomb.druid.color.StandardPalette;
import org.catacomb.druid.xtext.canvas.FontStore;
import org.catacomb.interlish.content.BasicTouchTime;

import java.util.ArrayList;

import java.awt.Font;
import java.awt.Color;

public class Guise {


    public String id;
    public ArrayList<Slot> slots;


    String fontFamily;
    String fontSize;
    String fontStyle;
    Color fontColor;

    boolean bUnderline;

    BasicTouchTime touchTime;

    BasicTouchTime cacheTime;


    Font cachedFont;

    static int icolor = 0;

    public Guise() {
        fontFamily = "serif";
        fontSize = "12";
        fontColor = Color.black;
        fontStyle = "plain";
        bUnderline = false;

        slots = new ArrayList<Slot>();
        cacheTime = new BasicTouchTime();
        touchTime = new BasicTouchTime();
    }


    public void setNextPaletteColor() {
        setFontColor(StandardPalette.getColor(icolor));
        icolor += 1;
    }


    public void touch() {
        touchTime.now();
    }


    // refac - don't want dep on canvas pkg;
    public Font getFont() {
        if (cachedFont == null || cacheTime.isBefore(touchTime)) {
            updateCache();
        }
        return cachedFont;
    }

    public Color getColor() {
        return fontColor;
    }

    private void updateCache() {
        cachedFont = FontStore.instance().getFont(fontFamily, fontStyle, "" + fontSize);


        cacheTime.now();
    }



    public void setID(String s) {
        id = s;
    }


    public String getID() {
        return id;
    }


    public Color getFontColor() {
        return fontColor;
    }


    public void setFontColor(Color c) {
        if (fontColor == null) {
            fontColor = Color.black;
        }
        fontColor = c;
    }


    public String getFontFamily() {
        return fontFamily;
    }



    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        touch();
    }



    public String getFontSize() {
        return fontSize;
    }



    public void setFontSize(String fs) {
        this.fontSize = fs;
        touch();
    }



    public String getFontStyle() {
        return fontStyle;

    }



    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
        touch();
    }


    public void setBoldFont() {
        setFontStyle("bold");
        touch();
    }


    public void setColorBlack() {
        setFontColor(Color.black);
    }


    public void setUnderline(boolean b) {
        bUnderline = b;
    }


    public boolean underline() {
        return bUnderline;
    }


    public void setColorDarkGreen() {
        setFontColor(new Color(0, 160, 0));

    }






}
