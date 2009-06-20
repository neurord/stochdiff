package org.catacomb.numeric.difnet.model;

import org.catacomb.numeric.difnet.NetState;
import org.catacomb.numeric.difnet.NetStructure;
import org.catacomb.numeric.difnet.StructureLink;
import org.catacomb.numeric.difnet.StructureNode;
import org.catacomb.report.E;



public class BasicNetStructure implements NetStructure {


    BasicStructureNode environmentNode;

    int nEnvironmentLink;

    public BasicStructureNode[] nodes;
    public BasicStructureLink[] links;



    public StructureNode[] getNodes() {
        return nodes;
    }


    public StructureLink[] getLinks() {
        return links;
    }



    public void setNodes(BasicStructureNode[] anp) {
        nodes = anp;
    }


    public void setLinks(BasicStructureLink[] alp) {
        links = alp;
    }



    public void setSurfaceCapacitance(double csurf) {
        if (environmentNode != null) {
            E.error(" - creatine new env node when already have one " + "BasicNetStructure");
        }

        // create an environment node and add a link from every node to the
        // environment that encapsulates the capacitance of its links to other
        // nodes


        int nn = nodes.length;
        int nl = links.length;

        BasicStructureNode[] nna = new BasicStructureNode[nn + 1];
        BasicStructureLink[] lka = new BasicStructureLink[nl + nn];

        environmentNode = new BasicStructureNode();
        environmentNode.setFixed(true);

        nEnvironmentLink = nn;

        for (int i = 0; i < nn; i++) {
            nna[i] = nodes[i];
        }
        nna[nn] = environmentNode;

        for (int i = 0; i < nn; i++) {
            nna[i].setArea(0.0);
        }

        for (int i = 0; i < nl; i++) {
            links[i].calculateArea();
        }


        for (int i = 0; i < nn; i++) {
            BasicStructureLink bsl = new BasicStructureLink(nna[i], environmentNode);
            double acta = nna[i].getArea();
            bsl.setActiveArea(acta);
            bsl.applyAreaCapacitance(csurf);
            lka[i] = bsl;
        }

        for (int i = 0; i < nl; i++) {
            lka[nn + i] = links[i];
        }


        nodes = nna;
        links = lka;

    }



    public void setEnvironmentValue(double d) {
        if (environmentNode == null) {
            setSurfaceCapacitance(0.); // just to create the env node;
        }
        environmentNode.setFixedValue(d);
    }


    public void setInitialValue(double d) {
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].setInitialValue(d);
        }
    }



    // POSERR bit dodgy - if there is a non-zero capacitance, should set the
    // capacitance first
    public void setSurfaceConductance(double gsurf) {
        if (environmentNode == null) {
            setSurfaceCapacitance(0);
        }

        for (int i = 0; i < nEnvironmentLink; i++) {
            links[i].applyAreaConductance(gsurf);
        }

    }



    public void setAxialConductance(double d) {

        for (int i = nEnvironmentLink; i < links.length; i++) {
            links[i].applyAxialConductance(d);

        }
    }



    /*
     * may also want some lookup methods or resolvers, and possibly put the
     * netdiffuserreference in here?
     *
     *
     * void diffuse(CcChannelNet mlnet, Timestep tstep) { if (netdif == null)
     * init(); netdif.diffuse(mlnet, null, tstep); }
     *
     */



    /**
     * makes a new net according to these properties. node and link arrays are
     * allocated matching the node and link properties arrays contained here and
     * filled by calling newInstance on each of the properties objects.
     *
     * @return the net
     */
    public NetState newState() {
        int nn = nodes.length;
        int nl = links.length;
        BasicStateNode[] stateNodes = new BasicStateNode[nn];
        BasicStateLink[] stateLinks = new BasicStateLink[nl];

        for (int i = 0; i < nn; i++) {
            nodes[i].setWork(i);
        }

        for (int i = 0; i < nn; i++) {
            stateNodes[i] = nodes[i].newState();
        }

        for (int i = 0; i < nl; i++) {
            BasicStructureLink lkp = links[i];
            stateLinks[i] = lkp.newState(stateNodes[lkp.getNodeA().getWork()], stateNodes[lkp.getNodeB().getWork()]);
        }
        return new BasicNetState(this, stateNodes, stateLinks);
    }



}
