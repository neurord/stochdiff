package org.catacomb.interlish.structure;



public interface TargetStore extends Visible {

    boolean has(String id);

    Object get(String id);

    ProgressReport getProgressReport(String id);

    PopulableMenu getSubMenu(String id);

    AssemblyEditor getAssemblyEditor();

    SaverLoader getModel();

    Button getButton(String id);

    Marketplace getMarketplace();

    void setStringValue(String name, String value);

    void setObjectValue(String name, Object value);

    Dialog getDialog(String string);

    TextField getTextField(String string);

    TextArea getTextArea(String string);

    Choice getChoice(String string);

    Toggle getToggle(String string);

    InfoReceiver getInfoReceiver();

    StringValueEditor getStringValueEditor(String string);

    DoubleValueEditor getDoubleValueEditor(String string);

    ColorValueEditor getColorValueEditor(String string);

    IntegerValueEditor getIntegerValueEditor(String string);

    void printAvailable();

}
