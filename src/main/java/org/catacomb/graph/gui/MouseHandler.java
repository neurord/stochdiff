package org.catacomb.graph.gui;



import java.awt.Graphics2D;



abstract class MouseHandler {

    final static int NONE = 0;
    final static int BUFFERED = 1;
    final static int FULL = 2;
    private int repaintStatus = NONE;

    private boolean active;


    private final static int OUT = -1;
    private final static int UNDECIDED = 0;
    private final static int IN = 1;
    private int claimStatus = UNDECIDED;




    MouseHandler() {
        active = true;
        setClaimUndecided();
    }


    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }


    public boolean isActive() {
        return active;
    }



    boolean motionAware() {
        return false;
    }
    @SuppressWarnings("unused")
    boolean motionChange(Mouse m) {
        return false;
    }




    int getRepaintStatus() {
        return repaintStatus;
    }


    void setRepaintStatus(int i) {
        repaintStatus = i;
    }

    void setFullRepaint() {
        repaintStatus = FULL;
    }




    void setClaimUndecided() {
        claimStatus = UNDECIDED;
    }

    void setClaimIn() {
        claimStatus = IN;
    }

    void setClaimOut() {
        claimStatus = OUT;
    }



    boolean isIn() {
        return (claimStatus == IN);
    }

    boolean isUndecided() {
        return (claimStatus == UNDECIDED);
    }

    boolean isOut() {
        return (claimStatus == OUT);
    }





    // used to decide whether it has control

    void clear() {
    }

    @SuppressWarnings("unused")
    void init(Mouse m) {
    }

    @SuppressWarnings("unused")
    void advance(Mouse m) {
    }

    @SuppressWarnings("unused")
    void release(Mouse m) {
    }





    @SuppressWarnings("unused")
    void echoPaint(Graphics2D g) {
    }




    // if handler has won control, then the following are called
    @SuppressWarnings("unused")
    void missedPress(Mouse m) {

    }
    @SuppressWarnings("unused")
    void applyOnDown(Mouse m) {
    }

    @SuppressWarnings("unused")
    void applyOnDrag(Mouse m) {
    }
    @SuppressWarnings("unused")
    void applyOnRelease(Mouse m) {
    }

}
