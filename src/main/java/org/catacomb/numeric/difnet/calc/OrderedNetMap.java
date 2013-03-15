package org.catacomb.numeric.difnet.calc;

import org.catacomb.numeric.difnet.*;



/**
 * OrderedNetMap - remapping of the geometry of a DiffusiveNet, ready for
 * diffusion calculations. It is composed of NetMapNodes and NetMapLinks which
 * correspond 1-1 to nodes and links in the DiffusiveNet, but only contain
 * quantities relevant to the structure and diffusion calculations.
 *
 * The structure is set on initialization. Therafter in calculations the state
 * of the DiffusiveNet is copied in, calculations performed, and the results
 * copied back to the DiffusiveNet. One OrderedNetMap can be used for many
 * DiffusiveNets if they all have the same structure.
 *
 * NB both points and Links have capacitances (volume / surface effects).
 * Typically one or the other will be zero - volume for charge, surface for
 * concentratin.
 *
 */


public final class OrderedNetMap {

    NetMapNode[] node;
    NetMapLink[] link;

    boolean acyclic;
    private boolean useIntrinsics;


    OrderedNetMap(NetStructure dn) {
        acyclic = true;

        StructureNode[] difnetNode = dn.getNodes();
        StructureLink[] difnetLink = dn.getLinks();


        int nnode = difnetNode.length;
        int nlink = difnetLink.length;
        for (int i = 0; i < nnode; i++) {
            difnetNode[i].setWork(i);
        }

        // make corresponding node and link arrays, where each link has refs
        // to its end nodes
        NetMapNode[] wkNode = new NetMapNode[nnode];
        NetMapLink[] wkLink = new NetMapLink[nlink];

        for (int i = 0; i < nnode; i++) {
            wkNode[i] = new NetMapNode();
            wkNode[i].peerIndex = i;
            wkNode[i].fixed = difnetNode[i].hasFixedValue(null);
        }

        for (int i = 0; i < nlink; i++) {
            wkLink[i] = new NetMapLink();
            wkLink[i].peerIndex = i;

            int iwka = difnetLink[i].getNodeA().getWork();
            wkLink[i].nodeA = wkNode[iwka];

            int iwkb = difnetLink[i].getNodeB().getWork();
            wkLink[i].nodeB = wkNode[iwkb];
        }


        // compile a list of which links go to or from each node;
        // *** max six links per node;
        // leave out links from fixed nodes: could be lots of these (bath to
        // everything)
        // and we don't need them

        NetMapLink[][] linksPerNode = new NetMapLink[nnode][6];

        linksPerNode[0] = new NetMapLink[nnode];
        // ADHOC - envmt node is always index 0 and can go to all the rest;


        int[] nlinksPerNode = new int[nnode];

        for (int i = 0; i < nlink; i++) {
            if (wkLink[i].nodeA.fixed) {

            } else {
                int ia = wkLink[i].nodeA.peerIndex;
                linksPerNode[ia][nlinksPerNode[ia]++] = wkLink[i];
            }

            if (wkLink[i].nodeB.fixed) {

            } else {
                int ib = wkLink[i].nodeB.peerIndex;
                linksPerNode[ib][nlinksPerNode[ib]++] = wkLink[i];
            }
        }



        // reorder the nodes, first the fixed ones, then a depth first
        // tree search
        node = new NetMapNode[nnode];
        for (int i = 0; i < nnode; i++) {
            wkNode[i].mark = false;
        }
        for (int i = 0; i < nlink; i++) {
            wkLink[i].mark = false;
            wkLink[i].flip = false;
        }
        // insert the fixed nodes first
        int[] iin = { 0 };
        for (int i = 0; i < nnode; i++) {
            if (wkNode[i].fixed && !wkNode[i].mark) {
                node[iin[0]++] = wkNode[i];
                wkNode[i].mark = true;
            }
        }

        // now recursively put in the free ones, only following links to
        // other nodes not in the list yet
        int ntree = 0;
        for (int i = 0; i < nnode; i++) {
            if (!wkNode[i].mark) {
                ntree++;
                recindex(node, iin, linksPerNode, wkNode[i]);
            }
        }

        // Util.p("put in trees, total=" + ntree);


        // tidy the nodes to get rid of any null refs;
        for (int i = 0; i < nnode; i++) {
            node[i].upLink = tidyLinkArray(node[i].upLink);
            node[i].downLink = tidyLinkArray(node[i].downLink);
        }


        // index the new points;
        for (int i = 0; i < nnode; i++) {
            node[i].index = i;
        }

        // there is nothing to be done for the links - wkLink has
        // references to all the NetMapLinks (no others were created), and
        // the links don't use indices but have direct refs to the nodes
        link = wkLink;
    }


    public boolean getUseIntrinsics() {
        return useIntrinsics;
    }


    public void Sp(String s) {
        System.out.println(s);
    }


    public void print() {
        Sp("ordered net map with " + node.length + " nodes, structure indexes in brackets ");
        for (int i = 0; i < node.length; i++) {
            NetMapNode nmn = node[i];
            Sp("node " + i + "(" + nmn.peerIndex + "), ndownlinks=" + nmn.downLink.length
               + ", nuplink=" + nmn.upLink.length);
            if (nmn.isFree()) {

            } else {
                Sp("fixed at " + nmn.value);
            }
            for (int j = 0; j < nmn.downLink.length; j++) {
                printLink("down ", nmn.downLink[j], j);
            }
            for (int j = 0; j < nmn.upLink.length; j++) {
                printLink("  up ", nmn.upLink[j], j);
            }
        }
    }


    public void printLink(String dir, NetMapLink lk, int lki) {
        Sp(dir + lki + "(" + lk.peerIndex + "),  nodeA=" + lk.nodeA.index + "(" + lk.nodeA.peerIndex
           + "), nodeB=" + lk.nodeB.index + "(" + lk.nodeB.peerIndex + "), g=" + lk.conductance
           + ", c=" + lk.capacitance);
    }


    NetMapNode[] getNodes() {
        return node;
    }


    NetMapLink[] tidyLinkArray(NetMapLink[] cain) {
        NetMapLink[] ca = cain;
        if (ca == null)
            return new NetMapLink[0];
        int n = 0;
        while (n < ca.length && ca[n] != null)
            n++;
        if (n < ca.length) {
            NetMapLink[] cb = ca;
            ca = new NetMapLink[n];
            for (int i = 0; i < n; i++)
                ca[i] = cb[i];
        }
        return ca;
    }


    private NetMapLink[] addLink(NetMapLink[] cain, NetMapLink c) {
        NetMapLink[] ca = cain;
        int n = 0;
        if (ca == null)
            ca = new NetMapLink[3];
        while (n < ca.length && ca[n] != null)
            n++;
        if (n == ca.length)
            ca = extendNetMapLinkArray(ca);
        ca[n] = c;
        return ca;
    }


    private NetMapLink[] extendNetMapLinkArray(NetMapLink[] ca) {
        int n = ca.length;
        NetMapLink[] cb = new NetMapLink[2 * n];
        for (int i = 0; i < n; i++)
            cb[i] = ca[i];
        return cb;
    }


    private void recindex(NetMapNode[] nodeArray, int[] inxt, NetMapLink[][] linksPerNode,
                          NetMapNode currentNode) {
        currentNode.mark = true;
        nodeArray[inxt[0]++] = currentNode;
        NetMapLink[] nbrs = linksPerNode[currentNode.peerIndex];

        // put in links to the fixed nodes first;
        for (int i = 0; i < nbrs.length && nbrs[i] != null; i++) {
            NetMapLink h = nbrs[i];
            if (!h.mark) {
                if (h.nodeB.fixed || h.nodeA.fixed) {
                    if (h.nodeB.fixed)
                        h.reverseEnds();

                    h.nodeA.upLink = addLink(h.nodeA.upLink, h);
                    h.nodeB.downLink = addLink(h.nodeB.downLink, h);
                    h.mark = true;
                }
            }
        }


        for (int i = 0; i < nbrs.length && nbrs[i] != null; i++) {
            NetMapLink h = nbrs[i];
            if (!h.mark) {
                h.mark = true;
                if (h.nodeA != currentNode)
                    h.reverseEnds();
                h.nodeA.upLink = addLink(h.nodeA.upLink, h);
                h.nodeB.downLink = addLink(h.nodeB.downLink, h);
                if (h.nodeB.mark) {
                    System.out.println("WARNING found link which breaks acyclic prop?" + " from peers "
                                       + h.nodeA.peerIndex + " to " + h.nodeB.peerIndex);
                    acyclic = false;
                } else {
                    recindex(nodeArray, inxt, linksPerNode, h.nodeB);
                }
            }
        }
    }


    boolean isAcyclic() {
        return acyclic;
    }


    void readState(NetState dn) {
        useIntrinsics = dn.useIntrinsics();

        StateNode[] difnetNode = dn.getNodes();
        for (int i = 0; i < node.length; i++) {
            node[i].readState(difnetNode[node[i].peerIndex], dn.getTime());
        }
        StateLink[] difnetLink = dn.getLinks();
        for (int i = 0; i < link.length; i++) {
            link[i].readState(difnetLink[link[i].peerIndex]);
        }
    }


    void writeState(NetState difnet) {
        StateNode[] difnetNode = difnet.getNodes();
        for (int i = 0; i < node.length; i++) {
            node[i].writeState(difnetNode[node[i].peerIndex]);
        }
    }
}
