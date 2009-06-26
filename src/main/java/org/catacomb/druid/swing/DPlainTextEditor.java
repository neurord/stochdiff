package org.catacomb.druid.swing;


import org.catacomb.druid.event.LabelActor;
import org.catacomb.report.E;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.StringTokenizer;


/* CLASSDOC
 They may occasionally pop up, and are probably worth a read.
 But, they are not insistent: they do not block other activity until
 you click OK.

 */


public class DPlainTextEditor extends DFrame implements LabelActor {

    static final long serialVersionUID = 1001;

    DTextArea ta;
    String context = "";
    String[] target;

    LabelActor lact;


    public DPlainTextEditor() {
        super("string array editor");
        E.deprecate();
        DPanel pp = new DPanel();
        pp.setLayout(new BorderLayout(14, 14));

        ta = new DTextArea(12, 40, DTextArea.SCROLLABLE);

        // tf.setTextActor(this);
        ta.setColumns(22);
        ta.setRows(20);
        // tf.addFocusListener(this);
        ta.setEditable(true);

        pp.add("Center", ta);

        DPanel pbot = new DPanel();
        pbot.setLayout(new FlowLayout(FlowLayout.CENTER));

        DButton[] cbs = { (new DButton("apply")), (new DButton("cancel")) };
        for (int i = 0; i < cbs.length; i++) {
            pbot.add(cbs[i]);
            cbs[i].setLabelActor(this);
        }
        pp.add("South", pbot);

        getContentPane().add("Center", pp);
        pack();
    }


    public void setLabelActor(LabelActor la) {
        lact = la;
    }


    public void setContext(String s) {
        context = s;
    }


    public String[] getStringArray() {
        String s = ta.getText();
        StringTokenizer st = new StringTokenizer(s, "\n");
        ArrayList<String> v = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String tok = st.nextToken().trim();
            if (tok.length() > 0) {
                v.add(tok);
            }
        }
        int n = v.size();
        String[] sa = new String[n];
        for (int i = 0; i < v.size(); i++) {
            sa[i] = v.get(i);
        }
        return sa;
    }



    public void labelAction(String sarg, boolean selected) {

        if (sarg.equals("OK")) {
            if (lact != null) {
                lact.labelAction("OK", true);
            }
            setVisible(false);

        } else if (sarg.equals("cancel")) {
            setVisible(false);

        }
    }


    public void setTarget(String[] sa) {
        target = sa;
        showContent();
    }



    public void showContent() {
        String[] sa = target;

        if (sa == null) {
            ta.setText("");

        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < sa.length; i++) {
                sb.append(sa[i]);
                sb.append("\n");
            }

            ta.setText(sb.toString());
        }
        ta.setVisible(true);
        setVisible(true);
    }

    public void setText(String s) {

    }


    public String getText() {
        return ("ERROR text editor missing code");
    }

}
