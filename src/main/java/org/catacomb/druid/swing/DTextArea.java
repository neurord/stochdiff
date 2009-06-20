package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.event.TextActor;
import org.catacomb.report.E;
import org.catacomb.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.event.FocusListener;

import java.awt.event.FocusEvent;


public class DTextArea extends DPanel implements DocumentListener, FocusListener {

    static final long serialVersionUID = 1001;

    public final static int BARE = 1;
    public final static int SCROLLABLE = 2;

    JTextArea jta;
    Font keepFont;

    TextActor textActor;

    DScrollPane jsp;

    LabelActor lableActor;

    boolean ignoreChanges = false;

    boolean changedWhileFocused;


    public DTextArea(String content) {
        this(20, 5, BARE);
        setText(content);
    }


    public DTextArea(int ia, int ib) {
        this(ia, ib, BARE);
    }


    public DTextArea(int ia, int ib, String content) {
        this(ia, ib, BARE);
        setText(content);
    }


    public DTextArea(int width, int height, int type) {

        int nrow = height;
        int ncol = width;

        if (nrow < 2) {
            nrow = 2;
        }

        if (ncol < 10) {
            ncol = 10;
        }


        setLayout(new BorderLayout());
        jta = new JTextArea(nrow, ncol);
        jta.setLineWrap(true);


        if (keepFont != null) {
            jta.setFont(keepFont);
        }

        if (type == SCROLLABLE) {
            jsp = new DScrollPane(jta);
            jsp.setNoBorder();

            add("Center", jsp);

        } else {
            add("Center", jta);
        }
        jta.getDocument().addDocumentListener(this);
        jta.addFocusListener(this);
    }


    public void setLineBorder(int icol) {
        setBorder(BorderFactory.createLineBorder(new Color(icol)));
    }


    public void addLine() {
        int nr = jta.getRows();
        jta.setRows(nr + 1);
    }


    public void removeLine() {
        int nr = jta.getRows();
        if (nr > 1) {
            jta.setRows(nr - 1);
        }
    }



    public void setBg(Color c) {
        jta.setBackground(c);
        jsp.setBackground(c);
        setBackground(c);
    }


    @SuppressWarnings("unused")
    public void setLabelActor(LabelActor lact) {
        System.out.println("NB label actor in text are is unused");
        (new Exception()).printStackTrace();
    }



    public void setEnabled(boolean b) {
        jta.setEnabled(b);
        jta.setEditable(b);
    }


    public void setFont(Font f) {
        if (jta != null) {
            jta.setFont(f);

        } else {
            keepFont = f;
        }
    }

    /*
       public void setScrollSize(int w, int h) {
          if (jsp != null)
             jsp.setScrollSize(w, h);
       }
    */

    public void setBackgroundColor(Color c) {
        jta.setBackground(c);
        setBackground(c);
    }


    public void setTextActor(TextActor ed) {
        textActor = ed;
    }


    public void setColumns(int nc) {
        jta.setColumns(nc);
    }


    public void setRows(int nc) {
        jta.setRows(nc);
    }


    /*
     * public Dimension getPreferredSize() { S.p("cta - jta pref size " +
     * jta.getPreferredSize()); return new Dimension(500, 180); }
     */

    public void changedUpdate(DocumentEvent d) {
        flagChange();
    }


    public void insertUpdate(DocumentEvent d) {
        flagChange();
    }


    public void removeUpdate(DocumentEvent d) {
        flagChange();
    }



    private void flagChange() {
        if (ignoreChanges) {

        } else {
            changedWhileFocused = true;
            if (textActor != null) {
                textActor.textChanged(null);
            }
        }
    }


    public void focusGained(FocusEvent fev) {
        changedWhileFocused = false;
    }


    public void focusLost(FocusEvent fev) {
        if (changedWhileFocused) {
            if (textActor != null) {
                textActor.textEdited(getText());
            }
        }

    }


    public String getText() {
        return jta.getText();
    }


    public void setText(String s) {
        ignoreChanges = true;
        jta.setText(s);
        ignoreChanges = false;
    }


    public void setEditable(boolean b) {
        jta.setEditable(b);
    }


    public void highlightLine(int highlight) {
        try {
            int c0 = jta.getLineStartOffset(highlight);
            int c1 = jta.getLineEndOffset(highlight);
            jta.setSelectionColor(new java.awt.Color(255, 102, 51));
            jta.setSelectionStart(c0);
            jta.setSelectionEnd(c1);
            jta.requestFocusInWindow();
        } catch (Exception ex) {
            E.warning("Exception highlighting text? " + ex + " want line " + highlight +
                      " from text " + jta.getText());
        }

    }

    public void clearHighlight() {

    }


    public void setAntialiased() {
        E.missing("setting aa in DTextArea");
        // jta.putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY, Boolean.TRUE);
    }






    @SuppressWarnings("unused") // TODO
    public void setPadding(int padding) {
        // TODO Auto-generated method stub

    }


    public void setFontSize(int fs) {
        setFont(new Font(null, Font.PLAIN, fs));
    }


    public void resizeUpToText() {
        String stxt = getText().trim();
        int nlines = StringUtil.countLines(stxt);
        if (nlines >= jta.getRows()) {
            jta.setRows(nlines+1);
            revalidate();
        }
    }




}
