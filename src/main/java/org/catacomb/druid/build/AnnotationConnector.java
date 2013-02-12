package org.catacomb.druid.build;

import org.catacomb.interlish.annotation.ControlPoint;
import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.content.*;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.lang.annotation.*;
import java.lang.reflect.Field;


public class AnnotationConnector {

    TargetStore targetStore;


    public AnnotationConnector(TargetStore tgs) {
        targetStore = tgs;
    }



    public void annotationConnect(Object ctrl) {

        int nanot = 0;
        for (Field fld : ctrl.getClass().getFields()) {
            Annotation[] aa = fld.getDeclaredAnnotations();
            if (aa != null && aa.length > 0) {
                nanot += aa.length;
                for (Annotation ant : aa) {

                    if (ant instanceof IOPoint) {
                        ioPointConnect(ctrl, fld, ((IOPoint)ant).xid());

                    } else if (ant instanceof Editable) {
                        editableConnect(ctrl, fld, ((Editable)ant).xid());

                    } else if (ant instanceof ControlPoint) {
                        controlConnect(ctrl, fld, ((ControlPoint)ant).xid());

                    } else {
                        E.warning("unhandled annotation " + ant);
                    }

                }
            }
        }


        int nta = 0;
        for (Field fld : ctrl.getClass().getDeclaredFields()) {
            Annotation[] aa = fld.getDeclaredAnnotations();
            if (aa != null) {
                nta += aa.length;
            }
        }
        if (nta > nanot) {
            E.shortError("Class " + ctrl.getClass() + " has unused annotations\n " +
                         "(anotatiosn of private fields) - shoud these be public fields?");
        }


    }


    private void editableConnect(Object ctrl, Field fld, String guiID) {
        Object fval = null;

        try {
            fval = fld.get(ctrl);
        } catch (Exception ex) {
            E.warning("cannot get field " + fld + " on " + ctrl);
        }

        if (fval == null) {
            E.warning("all editable fields should be set in the constructor - "  +
                      "not so for " + fld.getName() + " on " + ctrl);
        } else {

            Object cpt = targetStore.get(guiID);
            if (cpt == null) {
                E.error("No such cpt in target store : " + guiID);

            } else {

                setEditable(fval, cpt);

            }
        }
    }


    private void setEditable(Object fval, Object edtr) {
        if (fval instanceof StringValue && edtr instanceof StringValueEditor) {
            ((StringValueEditor)edtr).setStringValue((StringValue)fval);

        } else if (fval instanceof BooleanValue && edtr instanceof BooleanValueEditor) {
            ((BooleanValueEditor)edtr).setBooleanValue((BooleanValue)fval);

        } else if (fval instanceof IntegerValue && edtr instanceof IntegerValueEditor) {
            ((IntegerValueEditor)edtr).setIntegerValue((IntegerValue)fval);

        } else if (fval instanceof DoubleValue && edtr instanceof DoubleValueEditor) {
            ((DoubleValueEditor)edtr).setDoubleValue((DoubleValue)fval);

        } else if (fval instanceof ColorValue && edtr instanceof ColorValueEditor) {
            ((ColorValueEditor)edtr).setColorValue((ColorValue)fval);

        } else {
            E.error("cannot connect val to editor " + fval + " " + edtr);
        }
    }



    private void controlConnect(Object ctrl, Field fld, String guiID) {
        Object cpt = targetStore.get(guiID);

        if (cpt == null) {
            E.linkToWarning("No such cpt in target store : " + guiID + " when connecting " +
                            "controller ", ctrl);
            targetStore.printAvailable();

        } else {
            Object subctrl = null;
            if (cpt instanceof Druid) {
                subctrl = ((Druid)cpt).getController();
            } else if (cpt instanceof Controller) {
                subctrl = cpt;
            } else {
                E.error("cannot control connect " + cpt + " " + cpt.getClass().getName());
            }
            if (subctrl != null) {
                try {
                    fld.set(ctrl, subctrl);

                } catch (Exception ex) {
                    E.error("cannot set gui cpt in controller: " + guiID + " cpt is " + cpt
                            + " but field needs " + fld.getType());
                }
            }
        }
    }


    private void ioPointConnect(Object ctrl, Field fld, String guiID) {
        Object cpt = targetStore.get(guiID);


        if (cpt == null) {
            E.linkToWarning("No such cpt in target store : " + guiID + " when connecting " +
                            "controller ", ctrl);
            targetStore.printAvailable();

        } else {

            try {
                fld.set(ctrl, cpt);

            } catch (Exception ex) {
                E.error("cannot set gui cpt in controller: " + guiID + " cpt is " + cpt
                        + " but field needs " + fld.getType());
            }
        }

    }





}
