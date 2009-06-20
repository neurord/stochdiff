package org.catacomb.druid.xtext.edit;

import org.catacomb.druid.swing.DTextCanvas;
import org.catacomb.druid.swing.dnd.Region;
import org.catacomb.druid.swing.dnd.RegionBoard;
import org.catacomb.druid.xtext.base.WordBlock;
import org.catacomb.druid.xtext.canvas.FontStore;
import org.catacomb.druid.xtext.data.PageDataStore;
import org.catacomb.druid.xtext.data.XType;
import org.catacomb.druid.xtext.data.XTypeStore;
import org.catacomb.interlish.content.IntPosition;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;



import java.awt.Color;
import java.awt.Graphics2D;


public class XTypeTree extends RegionBoard
    implements TextPainter {

    XTypeStore typeStore;

    XType selectedType;
    XType hoverType;

    PageDataStore pageDataStore;


    public XTypeTree(DTextCanvas c) {
        super(c);

        typeStore = XTypeStore.instance();
        canvas.setTextPainter(this);
    }


    public IntPosition getScreenPosition() {
        return canvas.getScreenPosition();
    }


    public void setSelectedType(XType xt) {
        selectedType = xt;
    }


    public void repaint() {
        canvas.repaint();
    }


    public void paintText(Graphics2D g) {
        clearRegions();

        g.setColor(Color.black);
        int ioff = 26;
        int joff = 20;
        for (XType xt : typeStore.getTypes()) {

            g.setFont(FontStore.instance().getDefaultFont());
            drawType(g, ioff, joff, xt, "type");

            int xr = ioff - 18;
            if (xt.hasFields()) {
                if (xt.isExpanded()) {
                    g.drawString(" - ", xr, joff);
                    addRegion(xr, joff + 4, 18, 16, xt, "collapse", Region.CLICK);

                } else {
                    g.drawString(" + ", ioff - 18, joff);
                    addRegion(xr, joff + 4, 18, 16, xt, "expand", Region.CLICK);
                }
                joff += 16;

                if (xt.isExpanded()) {
                    g.setFont(FontStore.instance().getDefaultItalicFont());
                    for (XType subt : xt.getSubtypes()) {
                        drawType(g, ioff + 16, joff, subt, "subtype");
                        joff += 16;
                    }
                }
            } else {
                joff += 16;
            }
        }

        fullTextHeight = joff;
//      addRegion(2, getHeight()-2, getWidth()-4, getHeight()-4 - joff, null, "_global", Region.DROP);

    }





    private void drawType(Graphics2D g, int ioff, int joff, XType xt, String tag) {
        if (xt == selectedType) {
            g.setColor(Color.yellow);
            g.fillRect(0, joff - 12, canvas.getWidth(), 16);
        }

        g.setColor(Color.black);

        g.drawString(xt.getID(), ioff, joff);
        addRegion(ioff, joff + 4, 60, 16, xt, tag, Region.DRAG_OR_DROP);
    }





    public void regionClicked(Region reg) {
        String tag  = reg.getTag();

        if (tag != null) {
            if (tag.equals("expand")) {
                ((XType)reg.getRef()).expand();

            } else if (tag.equals("collapse")) {
                ((XType)reg.getRef()).collapse();

            } else if (tag.equals("type")) {
                selectedType = (XType)reg.getRef();

                notifyChange(selectedType);

            } else {
                E.error("doint know " + tag);
            }
        }
        repaint();
    }


    public void hover(Object oa) {
        if (oa instanceof XType) {

            hoverType = (XType)oa;
            hoverType.expand();
        } else {
            hoverType = null;
        }
    }




    public void dropOn(Object src, Region reg) {
        if ("_global".equals(reg.getTag())) {
            dropGeneral(src);

        } else {

            Object tgt = reg.getRef();

            if (src instanceof WordBlock && tgt instanceof XType) {

                WordBlock tbl = (WordBlock)src;
                XType xt = (XType)tgt;

                pageDataStore.addItem(xt, tbl.getExtendedText());
                while (tbl != null) {
                    tbl.setType(xt);
                    tbl = tbl.getLinkee();
                }

            } else {
                E.error("wrong type  -ignoring drop of " + src + " on " + tgt);
            }
            repaint();
        }

    }

    public void emptyDrop(Object src) {
        dropGeneral(src);
    }

    public void dropGeneral(Object src) {
        if (src instanceof TextDisplayed) {
            String txt = ((TextDisplayed)src).getDisplayText();
            XType xt = new XType(txt);
            typeStore.addIfNew(xt);

            if (src instanceof WordBlock) {
                ((WordBlock)src).weakSetType(typeStore.getTypeType());
            }

        } else {
            E.error("can only accept text displayed, not " + src);
        }
        repaint();
    }


    public void setPageDataStore(PageDataStore pdStore) {
        pageDataStore = pdStore;
    }



}
