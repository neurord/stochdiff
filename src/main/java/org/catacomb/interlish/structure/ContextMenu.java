package org.catacomb.interlish.structure;

import org.catacomb.be.Position;


public interface ContextMenu {


    public void setContext(String[] options, SelectionActor h);

    public void showAt(Position absolutePosition);


}
