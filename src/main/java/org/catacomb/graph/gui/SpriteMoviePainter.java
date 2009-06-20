package org.catacomb.graph.gui;

import org.catacomb.be.Direction;
import org.catacomb.be.Placement;
import org.catacomb.be.Position;
import org.catacomb.datalish.*;
import org.catacomb.interlish.content.Polypoint;


public class SpriteMoviePainter implements MoviePaintInstructor {

    SpriteAnimation movie;

    int lastPainted;

    int frameToPaint;


    public SpriteMoviePainter() {

    }

    public SpriteMoviePainter(SpriteAnimation san) {
        super();
        setMovie(san);
    }


    public void setMovie(SpriteAnimation sa) {
        movie = sa;
    }


    public void setFrame(int ifr) {
        frameToPaint = ifr;
    }

    public void advanceToFrame(int ifr) {
        frameToPaint = ifr;
        if (frameToPaint > lastPainted) {
            for (int i = lastPainted+1; i <= frameToPaint; i++) {
                SceneConfig scene = movie.getSceneConfig(i);

                SpriteEvents sevs = scene.getEvents();
                if (sevs != null) {
                    sevs.perform();
                }
            }
        }
    }


    public int getNFrames() {
        int ret = 0;
        if (movie != null) {
            ret = movie.getNPoint();
        }
        return ret;
    }

    public boolean antialias() {
        return false;
    }

    public void instruct(Painter p) {

        if (movie != null) {
            SceneConfig scene = movie.getSceneConfig(frameToPaint);
            SpritePlacement[] spa = scene.getPlacements();;
            SpriteStore spriteStore = movie.getSpriteStore();
            for (SpritePlacement sp : spa) {
                String sid = sp.getID();
                Placement pmt = sp.getPlacement();
                SpriteData sdat = spriteStore.getSprite(sid);

                paintSprite(p, sdat, pmt);

                // E.info("frame " + frameToPaint + " " + sid + " " + pmt);
            }
            lastPainted = frameToPaint;
        }
    }


    private void paintSprite(Painter p, SpriteData sdat, Placement pmt) {
        Position pos = pmt.getPosition();
        Direction dir = pmt.getDirection();
        for (SpritePart sp : sdat.getParts()) {
            Polypoint pl = new Polypoint(sp.copyXpts(), sp.copyYpts());
            pl.rotate(dir);
            pl.translate(pos);


            double[] xd = pl.getXPts();
            double[] yd = pl.getYPts();

            if (sp.open()) {
                p.setColor(sp.getLineColor());
                p.drawPolyline(xd, yd);

            } else if (sp.closed()) {
                p.setColor(sp.getLineColor());
                p.drawPolygon(xd, yd);

            } else {
                p.setColor(sp.getFillColor());
                p.fillPolygon(xd, yd);
                p.setColor(sp.getLineColor());
                p.drawPolygon(xd, yd);
            }
        }

        for (SpriteMarker sm : sdat.getMarkers()) {
            p.setColorWhite();
            Position mpos = sm.getPosition().copy();
            mpos.rotateTo(dir);
            mpos.shift(pos);
            p.fillCenteredOval(mpos, 2);
        }
    }


    public Box getLimitBox() {
        Box b = null;
        if (movie != null) {
            b = movie.getBox();
            SpriteStore spriteStore = movie.getSpriteStore();
            SceneConfig scene = movie.getSceneConfig(0);
            if (scene != null) {
                SpritePlacement[] spa = scene.getPlacements();
                if (spa != null) {
                    for (SpritePlacement sp : spa) {
                        String sid = sp.getID();
                        SpriteData sdat = spriteStore.getSprite(sid);
                        sdat.pushBox(b);
                    }
                }
            }
        }
        return b;
    }

    public String getFrameDescription(int ifr) {
        return movie.getFrameDescription(ifr);
    }




}
