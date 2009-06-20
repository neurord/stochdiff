package org.catacomb.druid.xtext.canvas;

import org.catacomb.druid.swing.dnd.RegionBoard;
import org.catacomb.druid.xtext.base.DMItem;

import java.util.ArrayList;

public class BoardRepainterDMItem extends DMItem {


    ArrayList<RegionBoard> boards;



    public BoardRepainterDMItem() {
        boards = new ArrayList<RegionBoard>();
    }




    public BoardRepainterDMItem(RegionBoard tb) {
        this();
        boards.add(tb);
    }


    public void childChanged(Object src) {
        for (RegionBoard rb : boards) {
            rb.repaint();
        }
    }




    public void addBoard(RegionBoard rb) {
        boards.add(rb);

    }

}
