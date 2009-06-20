package org.catacomb.druid.swing;


import java.awt.Color;
import java.awt.Graphics;

public class Relief {



    public static void drawUpButton(Graphics g, Color c,
                                    int x, int y, int w, int h) {

        g.drawLine(x,     y+h,   x+w,   y+h);
        g.drawLine(x+1,   y+h-1, x+w,   y+h-1);
        g.drawLine(x+w,   y,     x+w,   y+h);
        g.drawLine(x+w-1, y+1,   x+w-1, y+h);


        g.setColor(c.brighter());
        g.drawLine(x,     y,     x+w,   y);
        g.drawLine(x,     y+1,   x+w-1,   y+1);
        g.drawLine(x,     y,      x,   y+h);
        g.drawLine(x+1,   y,     x+1, y+h-1);


        g.setColor(c);
        g.fillRect(x+2, y+2, w-3, h-3);

    }

}
