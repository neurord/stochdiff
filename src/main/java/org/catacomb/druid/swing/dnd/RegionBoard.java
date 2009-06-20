package org.catacomb.druid.swing.dnd;

import org.catacomb.druid.swing.DTextCanvas;
import org.catacomb.interlish.structure.ChangeNotifiable;
import org.catacomb.report.E;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.dnd.DnDConstants;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


public class RegionBoard implements FocusListener, RegionListener {

    protected DTextCanvas canvas;

    ChangeNotifiable changeNotifiable;

    private ClickableRegionStore crStore;

    boolean inFocus;

    RegionDropTarget dropTarget;
    RegionDragSource dragSource;

    protected int fullTextHeight = 100;



    public RegionBoard(DTextCanvas c) {
        super();
        canvas = c;

        crStore = new ClickableRegionStore(c);
        crStore.setRegionListener(this);

        canvas.addFocusListener(this);

        dropTarget = new RegionDropTarget(this);


        dragSource = new RegionDragSource(this, DnDConstants.ACTION_COPY_OR_MOVE);

        canvas.setTransferHandler(new InternalTransferHandler());
    }


    public DTextCanvas getCanvas() {
        return canvas;
    }



    public void clearRegions() {
        crStore.clear();
    }

    public void requestFocus() {
        canvas.requestFocusInWindow();
    }


    public void setAntialias(boolean b) {
        canvas.setAntialias(b);
    }

    public void repaint() {
        canvas.repaint();
    }

    public int getWidth() {
        return canvas.getWidth();
    }

    public int getHeight() {
        return canvas.getHeight();
    }

    public void setChangeNotifiable(ChangeNotifiable cn) {
        changeNotifiable = cn;
    }

    public void notifyChange(Object obj) {
        if (changeNotifiable != null) {
            changeNotifiable.changed(obj);
        }
    }


    public void prePaintRegions(Graphics2D g) {
        if (inFocus) {
            g.setColor(Color.white);
            g.drawRect(1, 1, canvas.getWidth()-2, canvas.getHeight()-2);
        }

    }




    protected void addLengthenedRegion(int[] cachedPosition, Object b) {
        crStore.addLengthenedRegion(cachedPosition, b);
    }

    protected void addClickRegion(int[] xywh, Object b, String s) {
        crStore.addRegion(xywh, b, s,  Region.CLICK);
    }


    protected void addDragRegion(int[] xywh, Object b, String s) {
        crStore.addRegion(xywh, b, s, Region.DRAG);
    }

    protected void addDropRegion(int[] xywh, Object b, String s) {
        crStore.addRegion(xywh, b, s, Region.DROP);
    }

    protected void addDragOrDropRegion(int[] xywh, Object b, String s) {
        crStore.addRegion(xywh, b, s, Region.DRAG_OR_DROP);
    }

    protected void addRegion(int x, int y, int w, int h, Object obj, String s) {
        crStore.addRegion(x, y, w, h, obj, s, Region.CLICK);

    }

    protected void addRegion(int x, int y, int w, int h, Object obj, String s, int ity) {
        crStore.addRegion(x, y, w, h, obj, s, ity);

    }


    public Region getDragOverRegion(int x, int y) {
        crStore.dragOver(x, y);
        return crStore.getDragOverRegion();
    }


    public Region getDragOverRegion() {
        return crStore.getDragOverRegion();
    }


    public void dropOn(Object src, Region target) {
        E.info("item dropped on region " + src + " " + target);

    }


    public void regionClicked(Region reg) {
        E.override("click regoin " + reg);
    }


    boolean hasFocus() {
        return inFocus;
    }

    public void focusGained(FocusEvent e) {
        inFocus = true;
        repaint();
    }


    public void focusLost(FocusEvent e) {
        inFocus = false;
        repaint();
    }


    public Region getHoverRegion() {
        return crStore.getHoverRegion();
    }


    public void unecho(Region reg) {
        crStore.unecho(reg);
    }

    @SuppressWarnings("unused")
    public void dropGeneral(Object dropee) {
        E.override();

    }


    public int getFullTextHeight() {
        return fullTextHeight;
    }


}
