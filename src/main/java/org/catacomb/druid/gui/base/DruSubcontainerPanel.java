package org.catacomb.druid.gui.base;


import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.report.E;





public abstract class DruSubcontainerPanel extends DruPanel {



    public DruSubcontainerPanel() {

    }

    public void addPanel(DruPanel drup) {
        setColors(drup);
        subAddPanel(drup);
    }


    public void addDComponent(DComponent obj) {
        subAddDComponent(obj);
    }


    public void removeAll() {
        subRemoveAll();
    }


    public void addMenu(DruMenu menu) {
        E.missing();
    }


    public void addCardPanel(DruPanel drup) {
        E.missing();
    }

    @SuppressWarnings("unused")
    public void addPanel(String pos, DruPanel drup) {
        E.missing();
    }


    public void addDComponent(DComponent obj, Object constraints) {
        E.missing();
    }



    public abstract void subAddPanel(DruPanel drup);

    public abstract void subAddDComponent(DComponent dcpt);

    public abstract void subRemoveAll();

}
