
package org.catacomb.interlish.structure;

import java.util.ArrayList;


public interface VisibleState extends Touchable, IDd, Titled {

    ArrayList<VisibleState> getSubstates();


}
