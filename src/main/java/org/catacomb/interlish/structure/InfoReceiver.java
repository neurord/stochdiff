
package org.catacomb.interlish.structure;


public interface InfoReceiver extends Receiver {

    void receiveInfo(String s);

    void receiveInfo(String title, String text);

}
