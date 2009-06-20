package org.catacomb.interlish.structure;


public interface FlatDataStore {

    boolean contains(String name);

    void setString(String name, String svalue);

    void setBoolean(String name, boolean bvalue);

    void setInt(String name, int ivalue);

    void setDouble(String name, double dvalue);

    void setObject(String name, Object obj);



    String getString(String name);

    int getInt(String name);

    boolean getBoolean(String name);

    double getDouble(String name);

    Object getObject(String name);

    void dump();

}
