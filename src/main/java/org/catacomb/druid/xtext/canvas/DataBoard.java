package org.catacomb.druid.xtext.canvas;

import org.catacomb.druid.swing.DTextCanvas;
import org.catacomb.druid.swing.dnd.Region;
import org.catacomb.druid.swing.dnd.RegionBoard;
import org.catacomb.druid.xtext.base.WordBlock;
import org.catacomb.druid.xtext.data.*;
import org.catacomb.interlish.structure.TextDisplayed;
import org.catacomb.interlish.structure.TextPainter;


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;


public class DataBoard extends RegionBoard implements TextPainter {

    FontStore fontStore;

    PageDataStore pageDataStore;

    int width;
    int height;

    int itxt;
    int jtxt;



    public DataBoard(DTextCanvas dtc) {
        super(dtc);

        dtc.setTextPainter(this);

        fontStore = FontStore.instance();

    }



    public void paintText(Graphics2D g) {

        if (pageDataStore == null) {
            return;
        }

        width = canvas.getWidth();
        height = canvas.getHeight();

        g.setFont(fontStore.getActiveFont());
        g.setColor(Color.black);
        FontMetrics fm = g.getFontMetrics();


        itxt = 10;
        jtxt = 30;

        for (ArrayList<XItem> alxi : pageDataStore.getLists()) {
            XItem xi0 = alxi.get(0);
            XType xt = xi0.getType();

            g.setFont(fontStore.getH2Font());
            g.drawString(xt.getID(), itxt, jtxt);

            itxt += g.getFontMetrics().stringWidth(xt.getID()) + 6;
            itxt += 8;

            g.setFont(fontStore.getActiveFont());

            if (xt.hasFields()) {
                drawBracketedList(g, fm, xt.getFieldNames());
            }

            for (XItem xit : alxi) {
                jtxt += 18;
                itxt = 32;

                g.fillOval(20, jtxt - 8, 6, 6);

                g.setFont(fontStore.getBoldFont());

                String stxt = xit.getText();
                if (stxt == null || stxt.length() == 0) {
                    g.setColor(Color.red);
                    stxt = "- name -";
                    g.drawString(stxt, itxt, jtxt);
                    g.setColor(Color.black);

                } else {
                    g.setColor(Color.black);
                    g.drawString(xit.getText(), itxt, jtxt);
                }

                itxt += g.getFontMetrics().stringWidth(stxt) + 8;
                g.setFont(fontStore.getActiveFont());

                if (xt.hasFields()) {
                    for (XType ft : xt.getFields()) {
                        String sid = ft.getID();
                        String val = xit.getFieldValue(sid);
                        if (val == null) {
                            g.setColor(Color.red);
                            int w = fm.stringWidth(sid);
                            if (itxt + w > width) {
                                itxt = 48;
                                jtxt += 18;
                            }


                            // g.drawRect(itxt-2, jtxt - 9, w+4, 11);
                            g.drawLine(itxt, jtxt + 2, itxt + w, jtxt + 2);

                            addRegion(itxt, jtxt + 4, w, 16, xit, sid, Region.DROP);

                            g.drawString(sid, itxt, jtxt);
                            itxt += w;
                            itxt += 12;
                            g.setColor(Color.black);

                            // regionStore.addRegion(itxt, jtxt, w, 16, xit, sid);

                        } else {
                            val += ", ";
                            int w = fm.stringWidth(val);
                            if (itxt + w > width) {
                                itxt = 48;
                                jtxt += 18;
                            }
                            g.drawString(val, itxt, jtxt);
                            itxt += w;
                        }

                    }
                }
            }



            itxt = 10;
            jtxt += 25;
        }


        jtxt += 20;

        if (pageDataStore.hasRelations()) {
            g.setFont(fontStore.getH2Font());
            g.drawString("Relations", itxt, jtxt);
            jtxt += 20;

            for (XRelation xr : pageDataStore.getRelations()) {
                itxt = 10;
                String atxt = xr.getAText();
                if (atxt == null || atxt.length() == 0) {
                    g.setColor(Color.red);
                    g.drawRect(itxt, jtxt - 12, 50, 16);
                    addRegion(itxt, jtxt + 4, 50, 16, xr, "left", Region.DROP);
                    itxt += 50 + 10;

                } else {
                    g.setFont(fontStore.getActiveFont());
                    g.setColor(Color.black);
                    g.drawString(atxt, itxt, jtxt);
                    itxt += g.getFontMetrics().stringWidth(atxt) + 10;
                }

                g.setFont(fontStore.getDefaultItalicFont());
                g.setColor(Color.black);
                String tid = xr.getTypeID();
                g.drawString(tid, itxt, jtxt);
                itxt += g.getFontMetrics().stringWidth(tid) + 10;

                String btxt = xr.getBText();
                if (btxt == null || btxt.length() == 0) {
                    g.setColor(Color.red);
                    g.drawRect(itxt, jtxt - 12, 50, 16);
                    addRegion(itxt, jtxt + 4, 50, 16, xr, "right", Region.DROP);
                    itxt += 50 + 10;

                } else {
                    g.setFont(fontStore.getActiveFont());
                    g.setColor(Color.black);
                    g.drawString(btxt, itxt, jtxt);
                    itxt += g.getFontMetrics().stringWidth(btxt) + 10;
                }
                jtxt += 18;
            }


        }


        int ofth = fullTextHeight;
        fullTextHeight = jtxt + 10;

        if (Math.abs(fullTextHeight - ofth) > 10) {
            canvas.contentSizeChanged();
        }


    }



    private void drawBracketedList(Graphics2D g, FontMetrics fm, String[] sa) {
        int n = sa.length;
        for (int i = 0; i < n; i++) {
            String s = sa[i];
            if (i == 0) {
                s = "(" + s;
            }
            if (i < n - 1) {
                s = s + ", ";
            }
            if (i == n - 1) {
                s = s + ")";
            }

            int w = fm.stringWidth(s);
            if (itxt + w > width) {
                itxt = 20;
                jtxt += 18;
            }
            g.drawString(s, itxt, jtxt);
            itxt += w;
        }
    }



    public void dropOn(Object src, Region reg) {
        Object tgt = reg.getRef();

        String tag = reg.getTag();
        String txt = "err";
        if (src instanceof TextDisplayed) {
            txt = ((TextDisplayed)src).getDisplayText();
        }


        if (tgt instanceof XItem) {
            ((XItem)tgt).setField(tag, txt);

            if (src instanceof WordBlock) {
                applyType((WordBlock)src, XTypeStore.instance().getPropertyType());
            }

        } else if (tgt instanceof XRelation) {
            if (tag.equals("left")) {
                ((XRelation)tgt).setA(txt, src);
            } else {
                ((XRelation)tgt).setB(txt, src);
            }
            if (src instanceof WordBlock) {
                applyType((WordBlock)src, XTypeStore.instance().getThingType());
            }
        }
        repaint();
    }


    public void dropGeneral(Object src) {
        if (src instanceof XType) {
            XItem xit = new XItem((XType)src, "");
            pageDataStore.addItem(xit);


        } else if (src instanceof XRelationType) {
            XRelation xr = new XRelation((XRelationType)src);
            pageDataStore.addRelation(xr);

        }
        repaint();
    }


    private void applyType(WordBlock wbin, XType xt) {
        WordBlock wb = wbin;
        while (wb != null) {
            wb.setType(xt);
            wb = wb.getLinkee();
        }
    }



    public void emptyClick() {

    }




    public void setPageDataStore(PageDataStore pdStore) {
        pageDataStore = pdStore;
    }



}
