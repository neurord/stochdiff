package org.catacomb.druid.swing.dnd;

import org.catacomb.druid.swing.DTextCanvas;
import org.catacomb.report.E;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;



public class ClickableRegionStore extends RegionStore implements MouseListener, MouseMotionListener {

    DTextCanvas canvas;

    RegionListener regionListener;



    public ClickableRegionStore(DTextCanvas c) {
        super();
        canvas = c;
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
    }


    public void setRegionListener(RegionListener rl) {
        regionListener = rl;
    }


    public void mouseClicked(MouseEvent e) {
        canvas.requestFocus();
    }



    public void mousePressed(MouseEvent e) {
        press(e.getX(), e.getY());

    }





    public void mouseReleased(MouseEvent e) {
        Region reg = getPressRegion();
        if (reg != null) {
            if (regionListener != null) {
                regionListener.regionClicked(reg);
            } else {
                E.info("region clicked but no listener " + reg);
            }
        }

    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }


    public void mouseDragged(MouseEvent e) {

    }


    public void mouseMoved(MouseEvent e) {

        Region rpr = getHoverRegion();
        hoverOver(e.getX(), e.getY());

        Region rnw = getHoverRegion();

        if ((rpr == null && rnw != null) ||
                (rpr != null && rnw == null) ||
                (rpr != null && !(rpr == rnw))) {
            // somethings changed;

            Graphics g = canvas.getGraphics();
            if (rpr != null) {

                unecho(rpr);

            }

            if (rnw != null) {
                if (rnw.draggable()) {
                    g.setColor(Color.magenta);

                    g.drawRect(rnw.getX(), rnw.getY(), rnw.getW(), rnw.getH());
                    g.drawRect(rnw.getX()+1, rnw.getY()+1, rnw.getW()-2, rnw.getH()-2);

                } else if (rnw.clickable()) {
                    g.setColor(Color.orange);

                    g.drawRect(rnw.getX(), rnw.getY(), rnw.getW(), rnw.getH());
                    g.drawRect(rnw.getX()+1, rnw.getY()+1, rnw.getW()-2, rnw.getH()-2);


                }
            }


        }

    }



    public void unecho(Region r) {
        Rectangle rect = new Rectangle(r.getX(), r.getY(), r.getW()+1, r.getH()+1);
        canvas.paintImmediately(rect);
    }



    public void clearRegions() {
        clear();
    }







}
