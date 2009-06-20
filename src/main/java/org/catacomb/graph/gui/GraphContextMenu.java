package org.catacomb.graph.gui;

import org.catacomb.be.Position;
import org.catacomb.interlish.structure.ContextMenu;
import org.catacomb.interlish.structure.SelectionActor;
import org.catacomb.report.E;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class GraphContextMenu implements ContextMenu {

    WorldCanvas canvas;

    JPopupMenu pMenu;

    SelectionActor handler;

    int nel = 0;

    public GraphContextMenu(WorldCanvas wc) {
        canvas = wc;
        pMenu = new JPopupMenu();
    }


    public void itemSelected(String s) {
        if (handler == null) {
            E.warning("no handler for event " + s);

        } else {
            handler.selectionAction(this, s);
        }
    }


    public void setContext(String[] options, SelectionActor h) {
        pMenu.removeAll();
        nel = 0;
        handler = h;
        for (String s : options) {
            if (s.equals("---")) {
                pMenu.addSeparator();
            } else {
                JMenuItem jmi = new JMenuItem(s);
                jmi.addActionListener(new GCMHandler(this, s));
                pMenu.add(jmi);
                nel++;
            }
        }
    }


    public void showAt(Position pos) {
        if (nel > 0) {
            int[] ixy = canvas.getIntPosition(pos);
            pMenu.show(canvas, ixy[0], ixy[1]);
        }
    }




    class GCMHandler implements ActionListener {
        GraphContextMenu gcm;
        String svalue;
        GCMHandler(GraphContextMenu v, String s) {
            gcm = v;
            svalue = s;
        }

        public void actionPerformed(ActionEvent e) {
            gcm.itemSelected(svalue);

        }


    }


}
