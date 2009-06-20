
package org.catacomb.interlish.structure;

import java.util.ArrayList;


public interface RootController extends Controller {


    //   public void attachAssistantControllers(Controllable cbl);

    ArrayList<Controller> getCoControllers();


}
