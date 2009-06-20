package org.catacomb.druid.swing;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.catacomb.interlish.interact.DComponent;


public final class DScrollPane extends JScrollPane implements DComponent {

    static final long serialVersionUID = 1001;

    int w = 300;
    int h = 300;

    Color bgcolor;

    boolean sizeSpecified = false;


    public DScrollPane() {
        super();
        setHorizontalScrollbarNever();
        setNoBorder();
    }


    public DScrollPane(JComponent jcp) {
        super(jcp);
        setNoBorder();
    }

    public void setTooltip(String s) {
        setToolTipText(s);
    }


    public void setBg(Color c) {
        bgcolor = c;
        getViewport().setBackground(c);
        setBackground(c);

    }


    public void setScrollSize(int w, int h) {
        this.w = w;
        this.h = h;
        super.setSize(w, h);
        sizeSpecified = true;
    }


    public void setNoBorder() {
        setBorder(BorderUtil.makeZeroBorder());
    }


    public void scrollToBottom() {
        JComponent jc = (JComponent)(getViewport().getComponent(0));

        int height = jc.getHeight();
        jc.scrollRectToVisible(new Rectangle(0, height - 1, 1, height));
    }



    public void setViewDComponent(DComponent dcpt) {
        setViewportView((JComponent)dcpt);
    }


    /*
       public Dimension getPreferredSize() {
          if (sizeSpecified) {
             return new Dimension(w, h);
          }

          return super.getPreferredSize();
       }
    */

    public void setVerticalScrollBarAlways() {
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }


    public void setVerticalScrollbarAsNeeded() {
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    }


    public void setVerticalScrollbarAlways() {
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }


    public void setVerticalScrollbarNever() {
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    }


    public void setHorizontalScrollbarAsNeeded() {
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }


    public void setHorizontalScrollbarAlways() {
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    }


    public void setHorizontalScrollbarNever() {
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }



}
