package org.catacomb.numeric.phys;


public class Phys {

    public final static int BLACK = 0;
    public final static int WHITE = 256 * 256 * 256 - 1;
    public final static int RED = 255 * 256 * 256;
    public final static int GREEN = 255 * 256;
    public final static int BLUE = 255;
    public final static int YELLOW = RED + GREEN;
    public final static int MAGENTA = RED + BLUE;
    public final static int CYAN = BLUE + GREEN;
    public final static int GRAY = (195<<16) + (195<<8) + 195;
    public final static int LIGHTGRAY = (225<<16) + (225<<8) + 225;
    public final static int DARKGRAY = (100<<16) + (100<<8) + 100;
    public final static int VERYDARKGRAY = (30<<16) + (30<<8) + 30;
    public final static int MIDYELLOW = (220<<16) + (220<<8) + 0;
    public final static int DARKYELLOW = (180<<16) + (180<<8) + 0;
    public final static int ORANGE = (255<<16) + (180<<8) + 0;
    public final static int DARKORANGE = (220<<16) + (150<<8) + 0;
    public final static int BRIGHTORANGE = (255<<16) + (210<<8) + 0;



    public final static double electronCharge = 1.602e-19;
    public final static double boltzmannConstant = 1.381e-23;


}
