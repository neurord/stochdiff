
package org.catacomb.druid.gui.edit;



import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DTextArea;
import org.catacomb.report.E;


public class ExpandingTextAreaController  implements LabelActor {


    DTextArea dTextArea;

    public ExpandingTextAreaController(DTextArea dta) {
        dTextArea = dta;
    }


    public void labelAction(String s, boolean b) {
        if (s.equals("add")) {
            add();
        } else if (s.equals("remove")) {
            remove();
        } else {
            E.error("cannot act on " + s);
        }
    }

    public void add() {
        dTextArea.addLine();
        dTextArea.revalidate();

    }

    public void remove() {
        dTextArea.removeLine();
        dTextArea.revalidate();
    }



}
