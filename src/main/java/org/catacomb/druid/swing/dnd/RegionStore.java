package org.catacomb.druid.swing.dnd;



public class RegionStore {


    int[][] regs;
    Object[] refs;
    String[] tags;
    int[] actions;

    int nreg;

    Object active;
    int[] activeLims;
    String activeTag;


    Region dragOverRegion;
    Region hoverOverRegion;
    Region pressRegion;

    public RegionStore() {
        regs = new int[20][4];
        refs = new Object[20];
        tags = new String[20];
        actions = new int[20];
    }


    public void clear() {
        nreg = 0;
    }


    public void addRegion(int[] xywh, Object obj, String s, int acts) {
        addRegion(xywh[0], xywh[1], xywh[2], xywh[3], obj, s, acts);
    }


    public void addLengthenedRegion(int[] xywh, Object obj) {
        addRegion(xywh[0] - 6, xywh[1], xywh[2] + 12, xywh[3], obj, null, Region.CLICK);
    }



    public void addRegion(int x, int y, int w, int h, Object obj, String flav, int acts) {
        if (nreg == regs.length) {
            int nn = (3 * nreg) / 2;
            int[][] newregs = new int[nn][4];
            Object[] newrefs = new Object[nn];
            String[] newtags = new String[nn];
            int[] newactions = new int[nn];
            // keep memory together - any advantage?
            for (int i = 0; i < nreg; i++) {
                for (int j = 0; j < 4; j++) {
                    newregs[i][j] = regs[i][j];
                }
                newrefs[i] = refs[i];
                newtags[i] = tags[i];
                newactions[i] = actions[i];
            }
            regs = newregs;
            refs = newrefs;
            tags = newtags;
            actions = newactions;
        }

        regs[nreg][0] = x;
        regs[nreg][1] = x + w;
        regs[nreg][2] = y - h;
        regs[nreg][3] = y;

        refs[nreg] = obj;

        tags[nreg] = flav;

        actions[nreg] = acts;

        nreg++;
    }



    public Object activeRegion(int x, int y) {
        if (active != null && within(x, y, activeLims)) {
            // same as before;
        } else {

            active = null;
            for (int i = 0; i < nreg; i++) {
                if (within(x, y, regs[i])) {
                    active = refs[i];
                    activeLims = regs[i];
                    activeTag = tags[i];
                    break;
                }
            }
        }

        return active;
    }


    public int[] getActiveLimits() {
        return activeLims;
    }


    public String getActiveTag() {
        return activeTag;
    }


    private final boolean within(int x, int y, int[] rr) {
        return (x > rr[0] && x < rr[1] && y > rr[2] && y < rr[3]);
    }


    public Object getActive() {
        return active;
    }


    public boolean hasActive() {
        return (active != null);
    }


    public void clearActive() {
        active = null;
    }


    public void dragOver(int x, int y) {

        if (dragOverRegion != null && dragOverRegion.contains(x, y)) {
            // same as before;
        } else {
            dragOverRegion = null;
            for (int i = 0; i < nreg; i++) {
                if (within(x, y, regs[i])) {

                    dragOverRegion = new Region(regs[i], tags[i], refs[i], actions[i]);
                    break;
                }
            }
        }
    }


    public Region getDragOverRegion() {
        return dragOverRegion;
    }



    public void hoverOver(int x, int y) {

        if (hoverOverRegion != null && hoverOverRegion.contains(x, y)) {
            // same as before;
        } else {
            hoverOverRegion = null;
            for (int i = 0; i < nreg; i++) {
                if (within(x, y, regs[i]) && Region.canPress(actions[i])) {

                    hoverOverRegion = new Region(regs[i], tags[i], refs[i], actions[i]);
                    break;
                }
            }
        }
    }


    public Region getHoverRegion() {
        return hoverOverRegion;
    }


    public void press(int x, int y) {
        pressRegion = null;
        for (int i = 0; i < nreg; i++) {
            if (within(x, y, regs[i])) {

                pressRegion = new Region(regs[i], tags[i], refs[i], actions[i]);
                break;
            }
        }
    }



    public Region getPressRegion() {
        return pressRegion;
    }

}
