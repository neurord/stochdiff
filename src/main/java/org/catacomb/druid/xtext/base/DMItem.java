package org.catacomb.druid.xtext.base;


public class DMItem {


    public DMItem parent;


    public DMItem() {

    }


    public void notifyAppearanceChange() {
        propogateChange(this);
    }

    private void propogateChange(Object src) {
        childChanged(src);

        if (parent != null) {
            parent.propogateChange(src);
        }
    }

    public void setParent(DMItem dmi) {
        parent = dmi;
    }

    @SuppressWarnings("unused")
    public void childChanged(Object src) {

    }

}
