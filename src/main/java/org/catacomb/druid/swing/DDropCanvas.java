package org.catacomb.druid.swing;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;




import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.xtext.base.TextFloat;
import org.catacomb.druid.xtext.canvas.FontStore;
import org.catacomb.interlish.content.IntPosition;
import org.catacomb.report.E;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.KeyListener;

public class DDropCanvas extends JPanel
    implements MouseListener, KeyListener {
    private static final long serialVersionUID = 1L;



    BasicStroke bs1 = new BasicStroke((float)1.0);

    Color bgColor = new Color(248, 248, 248);
    Color liveBgColor = new Color(200, 255, 200);
    boolean antialias;
    int width;
    int height;

    int dragOffX;
    int dragOffY;

    FontStore fontStore;

    TextFloat dragOnFloat;

    LabelActor labelActor;

    IntPosition dragCorner;

    String text;
    int caretPos;
    int keyCode;

    Object dropee;

    public DDropCanvas() {
        setFont(new Font("sansserif", Font.PLAIN, 12));
        fontStore = FontStore.instance();
        dragCorner = new IntPosition();

        text = "";

        addMouseListener(this);
        addKeyListener(this);
        dropee = null;
    }


    public void setLabelActor(LabelActor la) {
        labelActor = la;
    }


    public void paintComponent(Graphics g0) {
        if (isActive()) {
            g0.setColor(liveBgColor);
        } else {
            g0.setColor(bgColor);
        }


        g0.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g = (Graphics2D)g0;

        if (antialias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        simpleStroke(g);

        paintText(g);

    }





    final void simpleStroke(Graphics2D g) {
        g.setStroke(bs1);
    }


    public void requestRepaint() {
        repaint();
    }




    public void paintText(Graphics2D g) {
        width = getWidth();
        height = getHeight();

//      g.setFont(fontStore.getActiveFont());
        g.setColor(Color.black);


        if (dragOnFloat != null) {
            paintFloat(g, dragOnFloat);
        }

        if (caretPos > text.length()) {
            caretPos = text.length();
        }
        g.setColor(Color.black);
        g.drawString(text, 6, 18);
        int xo = 6 + g.getFontMetrics().stringWidth(text.substring(0, caretPos));
        g.drawLine(xo, 6, xo, 20);
    }






    private void paintFloat(Graphics2D g, TextFloat b) {

        String txt = b.getText();
        int x = b.getX();
        int y = b.getY();
        int w = b.getWidth();
        if (w < 0) {
            w = fontStore.stringWidth(g, txt);
            b.setWidth(w);
        }
        //   int h = b.getHeight();

        g.setColor(Color.black);

        g.drawString(txt, x, y);
    }




    public IntPosition getScreenPosition() {
        Point p = getLocationOnScreen();
        IntPosition ret = new IntPosition((int)(p.getX()), (int)(p.getY()));
        return ret;
    }


    public void setSourceOffset(int x, int y) {
        dragOffX = x;
        dragOffY = y;
    }


    public void newTextDrag(String txt, IntPosition pos,  Object src) {
        dragOnFloat = new TextFloat(txt, pos, src);
        repaint();
    }


    public void moveDrag(IntPosition pos) {
        dragOnFloat.setPosition(pos);
        repaint();
    }


    private boolean isActive() {
        boolean ret = false;
        if (dragOnFloat != null) {
            int xc = dragOnFloat.getX();
            int yc = dragOnFloat.getY();
            if (xc > 0 && xc < width-10 && yc > 0 && yc < height) {
                ret = true;
            }

        }
        return ret;
    }


    public void drop() {
        if (isActive()) {
//        E.info("text dropped on drop box " + dragOnFloat.getText());
            setText(dragOnFloat.getText());
            dropee = dragOnFloat.getSource();
            dragOnFloat = null;
            reportAction();
        }
    }

    private void reportAction() {
        if (labelActor != null) {
            labelActor.labelAction(getText(), true);
        }

    }


    public void clear() {
        dragOnFloat = null;
        setText("");
        dropee = null;
        repaint();
    }


    public void setText(String s) {
        text = s;
        caretPos = 0;
        repaint();
    }


    public String getText() {
        return text;
    }


    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        requestFocus();
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }




    public void keyTyped(KeyEvent e) {
        if (e.isActionKey()) {
            E.info("action key " + KeyEvent.getKeyText(keyCode));

        } else {
            char c = e.getKeyChar();
            String sc = "" + c;
            if (Character.isLetter(c) || " -_".indexOf(sc) >= 0) {
                text = text.substring(0, caretPos) + sc +
                       text.substring(caretPos, text.length());
                caretPos += 1;
                dropee = null;
                repaint();
            }

        }
    }


    public void keyPressed(KeyEvent e) {
        keyCode = e.getKeyCode();
        switch (keyCode) {
        case KeyEvent.VK_LEFT:
            if (caretPos > 0) {
                caretPos -= 1;
                repaint();
            }
            break;
        case KeyEvent.VK_RIGHT:
            if (caretPos < text.length()) {
                caretPos += 1;
                repaint();
            }
            break;

        case KeyEvent.VK_BACK_SPACE:
            if (caretPos > 0) {
                text = text.substring(0, caretPos - 1) + text.substring(caretPos, text.length());
                caretPos -= 1;
                dropee = null;
                repaint();
            }
            break;

        case KeyEvent.VK_ENTER:
            reportAction();
            break;

        default:

        }
    }


    public void keyReleased(KeyEvent e) {
    }


    public Object getDropee() {
        return dropee;
    }

}

