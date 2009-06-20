

package org.catacomb.druid.swing;




public interface Menued {
    /**
     * Each option is a string, and the
     * list may not be sorted. In the gui, where several consecutive
     * items start with the same text followed by a colon, then this
     * is represented as a submenu where the submenu name comes from the
     * string up to the first colon, and the elements ofthe menu are
     * from the remainder. Hierarchical menus arrise from strings with
     * more htan one colon.
     */
    String[] getMenuOptions();

    void setMenuOptions(String[] sa);

}
