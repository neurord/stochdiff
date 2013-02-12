package org.catacomb.druid.build;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.catacomb.druid.gui.base.DruActionRelay;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.PanelWrapper;
import org.catacomb.interlish.service.AppPersist;
import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.interlish.service.ResourceLoader;
import org.catacomb.interlish.structure.*;
import org.catacomb.interlish.util.JUtil;
import org.catacomb.report.E;
import org.catacomb.util.PathUtil;



public class Druid implements Portal {

    String configPath;


    Object rootComponent;
    FrameShowable frameShowable;

    GUIStore guiStore;

    Controller baseController;
    Context parentContext;

    Context context;


    public Druid(String cp) {
        configPath = cp;

    }


    public Druid(String cp, Context ctx) {
        this(cp);
        parentContext = ctx;
    }


    public void whizzBang() {
        buildGUI();
        if (rootComponent instanceof ControllerSpecifier) {
            selfActivate();
        }
        packShow();

        context.getMarketplace().logUnresolved();

    }


    public void whizzNoBang() {
        buildGUI();
        if (rootComponent instanceof ControllerSpecifier) {
            selfActivate();
        }
    }





    public void buildGUI() {
        // parse the config into an abstract representation of the gui
        ResourceLoader resourceLoader = ResourceAccess.getResourceLoader();

        if (configPath.startsWith("com.") || configPath.startsWith("org.")) {
            // presumably an absolute path;
        } else {
            if (parentContext != null) {
                configPath = parentContext.getSourceLocation() + "." + configPath;

            } else {
                // requies resource loader to be able to find it - ie, the resource was
                //  there when the manifest was built;
            }
        }

        Object obj = resourceLoader.getResource(configPath, null);

        if (obj == null) {
            E.error("druid cannot find config " + configPath);
        }

        // parent context null for main frame;
        context = new Context(parentContext);
        context.setSourceLocation(PathUtil.getPackage(configPath));

        if (obj instanceof Realizer) {
            rootComponent = ((Realizer)obj).realize(context, new GUIPath());


            if (rootComponent instanceof FrameShowable) {
                frameShowable = (FrameShowable)rootComponent;
            }

        } else {
            E.fatalError("config does not define an application: " + obj + " " + configPath);
            AppPersist.forceExit();
        }
        // give access to this somehow TODO
        // context.dumpIDs();



        guiStore = context.getGUIStore();
    }



    public Object getRootComponent() {
        return rootComponent;
    }


    public DruPanel getMainPanel() {
        DruPanel ret = null;
        if (rootComponent instanceof DruPanel) {
            ret = (DruPanel)rootComponent;

        } else if (rootComponent instanceof PanelWrapper) {
            ret = ((PanelWrapper)rootComponent).getPanel();

        } else {
            E.error("cannot get panel - root=" + rootComponent + " " + baseController);
        }
        return ret;
    }


    public Controller getController() {
        return baseController;
    }


    public FrameShowable getFrameShowable() {
        return frameShowable;
    }


    public void packShow() {
        pack();
        show();
    }


    public void pack() {
        if (frameShowable != null) {
            frameShowable.pack();
        }
    }


    public void show() {
        if (frameShowable != null) {
            frameShowable.show();
        }
    }



    public void hide() {
        if (frameShowable != null) {
            frameShowable.hide();
        }
    }


    public void show(Object focusObject) {
        if (baseController != null && baseController instanceof Targetable) {
            ((Targetable)baseController).setTarget(focusObject);

        } else {
            E.error("cannot show " + focusObject + " with " + baseController);
        }
        show();
    }



    private void addController(ArrayList<Controller> controllers, Controller ctrl) {
        if (ctrl != null) {
            controllers.add(ctrl);

            if (baseController == null) {
                baseController = ctrl;
            }
        }
        if (ctrl instanceof RootController) {
            for (Controller coctrl : ((RootController)ctrl).getCoControllers()) {
                addController(controllers, coctrl);
            }
        }
    }



    public void selfActivate() {
        if (rootComponent instanceof ControllerSpecifier) {
            ArrayList<Controller> controllers = new ArrayList<Controller>();

            ControllerSpecifier cspec = (ControllerSpecifier)rootComponent;

            String path = cspec.getControllerPath();
            if (path != null && path.length() > 1) {
                if (path.indexOf(".") < 0) {
                    path = PathUtil.getPackage(configPath) + "." + path;
                }

                Controller ctrl = (Controller)(JUtil.newInstance(path));
                baseController = ctrl;
                addController(controllers, ctrl);
            }


            if (controllers.size() == 0) {
                E.warning("No controllers found in druid selfActivate");


            } else if (controllers.size() == 1) {
                attachSingleController(controllers.get(0));

            } else {
                attachMultipleControllers(controllers);
            }

        } else {
            E.warning("druid cannot self-activate " + rootComponent);
        }

        // used to log unresolved here - done in app now? but others?

    }



    public void attachSingleController(Controller ctrl) {
        if (ctrl instanceof RootController) {
            E.error("cannot attach root controller this way");
        }
        baseController = ctrl;

        if (ctrl instanceof InfoExporter) {
            ((InfoExporter)ctrl).setInfoReceiver(guiStore.getInfoReceiver());
        }

        if (guiStore == null) {
            E.warning("no guiStore for controller? " + ctrl);

        } else {
            AnnotationConnector anoc = new AnnotationConnector(guiStore.getTargetStore());

            anoc.annotationConnect(ctrl);

            if (ctrl instanceof Marketeer) {
                ((Marketeer)ctrl).setMarketplace(guiStore.getMarketplace());
            }

            DruActionRelay relay = new DruActionRelay(ctrl);

            for (Object obj : guiStore.getComponents()) {
                if (obj instanceof ActionSource) {
                    ((ActionSource)obj).setActionRelay(relay);
                }
            }
        }


        ctrl.attached();
    }




    public void setID(String id) {
        if (rootComponent instanceof IDable) {
            ((IDable)rootComponent).setID(id);
        } else {
            E.error("cannot set id on " + rootComponent);
        }
    }



    public void attachMultipleControllers(ArrayList<Controller> acc) {

        HashMap<String, ActionRelay> pathRelays = new HashMap<String, ActionRelay>();

        ActionRelay wcRelay = null;


        AnnotationConnector anoc = new AnnotationConnector(guiStore.getTargetStore());
        for (Controller ctrl : acc) {
            anoc.annotationConnect(ctrl);

            if (ctrl instanceof Marketeer) {
                ((Marketeer)ctrl).setMarketplace(guiStore.getMarketplace());
            }

            if (ctrl instanceof InfoExporter) {
                ((InfoExporter)ctrl).setInfoReceiver(guiStore.getInfoReceiver());
            }

            ActionRelay acr = new DruActionRelay(ctrl);

            String ss = "*";
            if (ctrl instanceof GUISourced) {
                ss = ((GUISourced)ctrl).getGUISources();
            }
            if (ss.trim().equals("*")) {
                if (wcRelay == null) {
                    wcRelay = acr;
                } else {
                    E.error("duplicate wildcard controllers " + ctrl);
                }

            } else {
                for (String pth : Scope.getFullPaths(ss)) {
                    pathRelays.put(pth, acr);
                }
            }
        }



        HashMap<String, ArrayList<Object>> hmanon = guiStore.getAnonymousComponentMap();
        HashMap<String, Object> hmid = guiStore.getIdentifiedComponentMap();

        for (Map.Entry<String, Object> me : hmid.entrySet()) {
            Object val = me.getValue();

            if (val instanceof ActionSource) {
                ActionRelay ar = bestRelay(me.getKey(), pathRelays, wcRelay);
                ((ActionSource)val).setActionRelay(ar);
            }
        }



        for (Map.Entry<String, ArrayList<Object>> me : hmanon.entrySet()) {
            ActionRelay ar = bestRelay(me.getKey(), pathRelays, wcRelay);

            for (Object val : me.getValue()) {
                if (val instanceof ActionSource) {
                    ((ActionSource)val).setActionRelay(ar);
                }
            }
        }

        for (Controller ctrl : acc) {
            ctrl.attached();
        }
    }




    // REFAC could be improved;
    private ActionRelay bestRelay(String path, HashMap<String, ActionRelay> map, ActionRelay wcRelay) {
        ActionRelay ret = null;

        if (map.containsKey(path)) {
            ret = map.get(path);
            // E.info("got exact relay for " + path);
        } else {
            int ilm = 0;
            for (String sp : map.keySet()) {
                if (path.startsWith(sp) && sp.length() > ilm) {
                    ilm = sp.length();
                    ret = map.get(sp);
                    // E.info("considering relay " + sp + " for " + path);
                }
            }
        }

        if (ret == null && wcRelay != null) {
            ret = wcRelay;
            // E.info("falling back on wildcard for " + wcRelay);
        }

        //   E.info("best relay for " + path + " is " + ret);

        return ret;
    }


    public void setModal(boolean b) {
        if (rootComponent instanceof Dialog) {
            ((Dialog)rootComponent).setModal(b);

        } else {
            E.error("cannot set modal on " + rootComponent.getClass().getName());
        }
    }

    public boolean isShowing() {
        boolean ret = false;
        if (rootComponent instanceof Dialog) {
            ret = ((Dialog)rootComponent).isShowing();
        } else {
            E.missing();
        }
        return ret;
    }


    public int[] getXY() {
        int[] ret = {100, 100};
        if (frameShowable != null) {
            ret = frameShowable.getLocation();
        } else if (rootComponent instanceof Dialog) {
            ret = ((Dialog)rootComponent).getLocation();
        } else if (rootComponent instanceof PanelWrapper) {
            ret = ((PanelWrapper)rootComponent).getPanel().getLocation();
        } else {
            E.warning("cannot get position for " + rootComponent);
        }
        return ret;
    }


}
