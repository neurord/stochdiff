package org.catacomb.druid.swing;

import org.catacomb.druid.swing.dnd.TextFieldDropTarget;
import org.catacomb.interlish.structure.TextDisplayed;
import org.catacomb.report.E;





public class DDropTextField extends DTextField {
    private static final long serialVersionUID = 1L;


    TextFieldDropTarget dropTarget;

    Object dropee;


    public DDropTextField(String s, int w) {
        super(s, w);

        //   setTransferHandler(new InternalTransferHandler());


        getJTextField().setDragEnabled(true);
        dropTarget = new TextFieldDropTarget(getJTextField(), this);

    }


    public void setDropee(Object obj) {
        dropee = obj;
        if (dropee instanceof TextDisplayed) {
            setText(((TextDisplayed)obj).getDisplayText());
        } else {
            E.error("need text displayed, not " + obj);
        }
        reportAction();
        //   repaint();
    }



    public Object getDropee() {
        return dropee;
    }





}
