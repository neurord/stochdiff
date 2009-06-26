package org.catacomb.druid.xtext.canvas;

import org.catacomb.druid.swing.DTextCanvas;
import org.catacomb.druid.swing.dnd.Region;
import org.catacomb.druid.swing.dnd.RegionBoard;
import org.catacomb.druid.xtext.base.*;
import org.catacomb.interlish.structure.TextPainter;
import org.catacomb.report.E;


import java.awt.Color;
import java.awt.Graphics2D;



public class TextBoard extends RegionBoard
    implements TextPainter {

    Block rootBlock;

    FontStore fontStore;

    int width;
    int height;

    int itxt;
    int jtxt;

    KeyWriter keyWriter;

    public final static String PUNCTUATION = " .,;:?!\t\n";


    private boolean nextIsLinked;



    public TextBoard(DTextCanvas dtc) {
        super(dtc);

        canvas.setTextPainter(this);


        fontStore = FontStore.instance();

        keyWriter = new KeyWriter(this);
        canvas.addKeyListener(keyWriter);


        canvas.setFocusable(true);
        requestFocus();

    }


    public void setBlock(Block rb) {
        rootBlock = rb;

        rootBlock.setParent(new BoardRepainterDMItem(this));

        keyWriter.setCaretBlock(firstTextBlock());

        canvas.repaint();
    }



    public TextBlock firstTextBlock() {
        TextBlock ret = null;
        Block b = rootBlock;
        while (b.hasNext()) {
            b = b.next();
            if (b instanceof TextBlock) {
                ret = (TextBlock)b;
                break;
            }
        }
        return ret;

    }



    public void repaint() {
        canvas.repaint();
    }


    public void paintText(Graphics2D g) {
        clearRegions();

        width = canvas.getWidth();
        height = canvas.getHeight();

        nextIsLinked = false;

        g.setFont(fontStore.getActiveFont());
        g.setColor(Color.black);

        itxt = 10;
        jtxt = 30;

        if (rootBlock != null) {
            paintDoc(g);
            if (keyWriter.hasCaret()) {
                paintCaret(g);
            } else {
                E.info("not painting caret");
            }
        }

        int ofth = fullTextHeight;
        fullTextHeight = jtxt + 10;

        if (Math.abs(fullTextHeight - ofth) > 10) {
            canvas.contentSizeChanged();
        }
    }



    private void paintDoc(Graphics2D g) {
        Block b = rootBlock;

        while (b.hasNext()) {
            b = b.next();
            if (b instanceof TextBlock) {
                paintBlock(g, (TextBlock)b);
            }
        }
    }



    private void paintCaret(Graphics2D g) {

        TextBlock caretBlock = keyWriter.getCaretBlock();
        int caretPos = keyWriter.getCaretPos();

        int w = 0;
        if (caretBlock instanceof NewlineBlock) {

        } else {
            String txt = caretBlock.getText();
            String part = txt.substring(0, caretPos);
            w = fontStore.stringWidth(g, part);
        }

        int[] xywh = caretBlock.getCachedPosition();
        g.setColor(Color.black);
        int xo = xywh[0] + w;
        int yo = xywh[1];
        if (caretBlock instanceof NewlineBlock && caretPos > 0) {
            xo = 10;
            yo += 16;
        }
        g.drawLine(xo, yo, xo, yo - xywh[3]);
    }



    private void paintBlock(Graphics2D g, TextBlock b) {
        if (b instanceof NewlineBlock) {
            b.setCachedPosition(itxt, jtxt, 0, 16);
            itxt = 10;
            jtxt += 16;

        } else {
            Guise guise = b.getGuise();
            g.setFont(guise.getFont());
            g.setColor(guise.getColor());


            String txt = b.getText();

            int w = g.getFontMetrics().stringWidth(txt);

            if (itxt + w > width && b instanceof WordBlock) {
                itxt = 10;
                jtxt += 16;
            }

            b.setCachedPosition(itxt, jtxt + 4, w, 16);

            if (b instanceof WordBlock) {
                g.drawString(txt, itxt, jtxt);

                if (guise.underline()) {
                    g.drawLine(itxt, jtxt + 2, itxt + w, jtxt + 2);
                }


                addDragRegion(b.getCachedPosition(), b, null);
                nextIsLinked = ((WordBlock)b).hasLinkee();

            } else if (b instanceof PunctuationBlock) {
                g.drawString(txt, itxt, jtxt);
                if (nextIsLinked) {
                    g.setColor(Color.blue);
                    //       g.drawLine(itxt-4, jtxt+2, itxt+w+4, jtxt+2);
                    g.drawLine(itxt-4, jtxt+1, itxt+w+4, jtxt+1);
                    //       g.fillOval(itxt + w / 2 - 3, jtxt - 6, 4, 4);
                    nextIsLinked = false;
                }

                if (txt.trim().equals("")) {
                    addLengthenedRegion(b.getCachedPosition(), b);
                }
            }

            itxt += w;
            // itxt += 4;
        }
    }


    public void regionClicked(Region reg) {
        Object src = reg.getRef();


        if (src instanceof PunctuationBlock) {
            PunctuationBlock pb = (PunctuationBlock)src;
            WordBlock pr = pb.getPreviousWordBlock();
            WordBlock pn = pb.getNextWordBlock();
            pr.toggleLink(pn);
        } else {
            E.warning("whats this ? " + src);
        }
        repaint();
    }
    public void dropOn(Object src, String txt, Object tgt, String tag) {

    }

}
