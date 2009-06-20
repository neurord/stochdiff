package org.catacomb.graph.gui;


public interface MoviePaintInstructor extends PaintInstructor {

    public void advanceToFrame(int ifr);

    public void setFrame(int ifr);

    public int getNFrames();

    public String getFrameDescription(int ifr);

}
