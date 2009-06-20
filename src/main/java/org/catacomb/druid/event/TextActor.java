

package org.catacomb.druid.event;


public interface TextActor extends FocusActor {

    void textChanged(String stxt); // every change

    void textEntered(String txt); // drop or return key press

    void textEdited(String txt); // lost focus

}
