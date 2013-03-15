package org.catacomb.interlish.structure;




public interface Marketplace {

    void addProducer(String modality, Producer p, String flavor);


    void addConsumer(String modality, Consumer c, String flavor);


    void addReceiver(String modality, Receiver rec, String flavor);


    // supplier will get you the flavor you ask for
    void addSupplier(String modality, Supplier sup);


    // providers are like producers, but don't care if their product is unused
    void addProvider(String modality, Provider p, String flavor);


    public void addVisible(String modality, Visible vbl, String flavor);


    public void addViewer(String modality, Viewer vwr, String flavor);

    public void logUnresolved();

    public Marketplace global();

}