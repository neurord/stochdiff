package org.catacomb.druid.xtext.edit;

import org.catacomb.druid.swing.DTextCanvas;
import org.catacomb.druid.swing.dnd.Region;
import org.catacomb.druid.swing.dnd.RegionBoard;
import org.catacomb.druid.xtext.base.WordBlock;
import org.catacomb.druid.xtext.canvas.FontStore;
import org.catacomb.druid.xtext.data.PageDataStore;
import org.catacomb.druid.xtext.data.XRelation;
import org.catacomb.druid.xtext.data.XRelationType;
import org.catacomb.druid.xtext.data.XTypeStore;
import org.catacomb.interlish.structure.TextDisplayed;
import org.catacomb.interlish.structure.TextPainter;
import org.catacomb.report.E;


import java.awt.Color;
import java.awt.Graphics2D;


public class XRelationTree extends RegionBoard
    implements TextPainter {

    XTypeStore typeStore;

    XRelationType selectedType;

    PageDataStore pageDataStore;


    public XRelationTree(DTextCanvas c) {
        super(c);
        typeStore = XTypeStore.instance();
        canvas.setTextPainter(this);

    }


    public void setPageDataStore(PageDataStore pdStore) {
        pageDataStore = pdStore;
    }


    public void setSelectedType(XRelationType xt) {
        selectedType = xt;
    }



    public void repaint() {
        canvas.repaint();
    }


    public void paintText(Graphics2D g) {

        g.setColor(Color.black);

        int joff = 20;
        for (XRelationType xr : typeStore.getRelationTypes()) {

            g.setFont(FontStore.instance().getDefaultFont());

            drawRelation(g, joff, xr);

            joff += 20;
        }

    }






    private void drawRelation(Graphics2D g, int joff, XRelationType xr) {
        int cw = canvas.getWidth();
        if (xr == selectedType) {
            g.setColor(Color.yellow);
            g.fillRect(0, joff - 12, canvas.getWidth(), 16);
        }

        g.setColor(Color.black);

        int txtoff = 80;

        g.drawString(xr.getID(), txtoff, joff);

        int sw = g.getFontMetrics().stringWidth(xr.getID());

        addRegion(txtoff, joff+4, sw, 16, xr, "main", Region.DRAG_OR_DROP);

        addRegion(5, joff + 4, txtoff-5, 16, xr, "left", Region.DROP);

        int w = cw - (txtoff - sw - 10);
        if (w > 100) {
            w = 100;
        }
        addRegion(txtoff+sw+5, joff + 4, w, 16, xr, "right", Region.DROP);
    }




    public void dropOn(Object src, Region reg) {
        Object tgt = reg.getRef();
        String tag = reg.getTag();
        String txt = "err";
        if (src instanceof TextDisplayed) {
            txt = ((TextDisplayed)src).getDisplayText();
        } else {
            E.error("wrong typ drop " + src);
        }

        if (tgt instanceof XRelationType) {
            XRelationType xrt = (XRelationType)tgt;
            XRelation xr = new XRelation(xrt);

            if (tag.equals("left")) {
                xr.setA(txt, src);
            } else if (tag.equals("right")) {
                xr.setB(txt, src);
            }
            pageDataStore.addRelation(xr);

            if (src instanceof WordBlock) {
                ((WordBlock)src).weakSetType(typeStore.getThingType());

            }
        }
        repaint();
    }


    public void regionClicked(Region reg) {
        Object src = reg.getRef();
        if (src instanceof XRelationType) {
            selectedType = (XRelationType)src;
            notifyChange(selectedType);
        }
    }


    public void dropGeneral(Object src) {
        String txt = null;
        if (src instanceof TextDisplayed) {
            txt = ((TextDisplayed)src).getDisplayText();
        } else {
            E.error("wrong typ drop " + src);
        }



        if (txt != null && txt.length() > 0) {
            XRelationType xrt = new XRelationType(txt);
            typeStore.addIfNew(xrt);
        }
        repaint();

    }



}
