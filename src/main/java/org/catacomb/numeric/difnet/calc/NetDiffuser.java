package org.catacomb.numeric.difnet.calc;

import org.catacomb.be.Timestep;
import org.catacomb.numeric.difnet.DiffusibleQuantity;
import org.catacomb.numeric.difnet.DiffusionCalculator;
import org.catacomb.numeric.difnet.NetState;
import org.catacomb.numeric.difnet.NetStructure;
import org.catacomb.numeric.math.Matrix;


/**
 * CcNetDiffuser computs diffusion or sparse or dense networks, with access via
 * the NetDiffuser interface. It does not require any special structure in the
 * network, but instead makes its own copy of the network geometry and
 * rearranges that for an in-place sparse diffusion solution (aka Hines method)
 * copying data in and out from the supplied DiffusiveNet via theresulting
 * permutation.
 *
 * Its only state information is the network geometry, so one CcNetDiffuser can
 * be used for networks derived from the same DiffusiveNetProperties object.
 *
 * Typical use is for the network builder to prodice a DiffusiveNetProperties
 * object (such as CcDiffusiveNetProperties), Which makes a CcNetDiffuser of
 * itself, and exports DiffusiveNet instances as required. When such an instance
 * is required to update, it calls the properties object which runs
 * <code>diffuse</code> from its NetDiffuser on the instance.
 */


public class NetDiffuser implements DiffusionCalculator {

    int icall = 0;


    final static int IOK = 0;
    final static int IERROR = 1;


    OrderedNetMap orderedNetMap;


    /**
     * creates and initialises a CcNetDiffuser for any diffusive nets sharing the
     * same diffusiveNetProperties.
     *
     * @param dnetprop
     *           the shared properties for nets to be handled by this diffuser.
     *
     */
    public NetDiffuser(NetStructure dnetprop) {
        init(dnetprop);
    }


    /**
     * initialise the internal mapping of the network geometry. an OrderedNetMap
     * is constructed specific to the given network structure. It is ordered such
     * as to minimise the work in any subsequent diffusion calculation.
     *
     * @param dnetprop
     *           the shared properties for nets to be handled by this diffuser.
     */
    public void init(NetStructure dnetprop) {
        orderedNetMap = new OrderedNetMap(dnetprop);
    }


    public void printNet() {
        orderedNetMap.print();
    }


    public void printNet(NetState ns) {
        orderedNetMap.readState(ns);
        orderedNetMap.print();
    }



    /**
     * Run the diffusion calculation for the supplied network, diffusible
     * quantity and timestep. The values in the DiffusiveNet are read with
     * <code> getValue(DiffusibleQuantity)</code> and set after the calculation
     * with <code> setValue(DiffusibleQuantity, double)</code>
     *
     * @param difnet
     *           the DiffusiveNet to evolve. Note that its properties object must
     *           be the same as that used to initialise the net diffuser.
     * @param dq
     *           the diffusible quantity to use. One net may contain several
     *           different diffusing quantitites - membrane potential, ions,
     *           indicators and others. NB as yet dq is ignored ***
     * @param timestep
     *           the timestep to be taken
     */
    public void advance(NetState difnet, DiffusibleQuantity dq, Timestep tstep) {

        double dt = tstep.getDeltaT();

        difnet.setTime(difnet.getTime() + dt);


        if (difnet.isError()) {
            return;
        }

        orderedNetMap.readState(difnet);

        int ires = IOK;

        if (orderedNetMap.isAcyclic() && !difnet.forceFullMatrix()) {
            ires = sparseNetDiffuse(orderedNetMap, dt);

        } else {
            ires = fullNetDiffuse(orderedNetMap, dt);
        }


        if (ires == IERROR) {
            orderedNetMap.print();
            difnet.setError();

        } else {
            orderedNetMap.writeState(difnet);
        }

        icall++;

    }



    private int fullNetDiffuse(OrderedNetMap ordNM, double dt) {

        int iret = IOK;

        NetMapNode[] node = ordNM.getNodes();
        int nnode = node.length;

        double[] rhs = new double[nnode];
        double[][] a = new double[nnode][nnode];

        boolean useIntrinsics = ordNM.getUseIntrinsics();

        for (int i = 0; i < nnode; i++) {
            NetMapNode p = node[i];
            if (p.isFree()) {
                rhs[i] = p.appliedFlux * dt;

                // conductances to nodes earlier in list
                int ndownLink = p.downLink.length;
                for (int j = 0; j < ndownLink; j++) {
                    NetMapLink c = p.downLink[j];
                    double gdt = dt * c.conductance;
                    rhs[i] += gdt * (c.nodeA.value - p.value - c.drive);
                    double cpgt = c.capacitance + gdt;
                    a[i][i] += cpgt;
                    a[i][c.nodeA.index] = -cpgt;

                    if (useIntrinsics) {
                        rhs[i] += dt * c.current; // POSERR
                    }
                }

                // and to nodes later in list
                int nupLink = p.upLink.length;
                for (int j = 0; j < nupLink; j++) {
                    NetMapLink c = p.upLink[j];
                    double gdt = dt * c.conductance;
                    rhs[i] += gdt * (c.nodeB.value - p.value + c.drive);
                    double cpgt = c.capacitance + gdt;
                    a[i][i] += cpgt;
                    a[i][c.nodeB.index] = -cpgt;

                    if (useIntrinsics) {
                        rhs[i] -= dt * c.current; // POSERR
                    }
                }

                if (p.appliedConductance > 0.) {
                    double gdt = dt * p.appliedConductance;
                    rhs[i] += gdt * (p.appliedDrive - p.value);
                    a[i][i] += gdt;
                }

            } else {
                // redundant equation. Could economise by leaving these
                // out entirely;
                p.value = p.appliedValue;
                a[i][i] = 1.;
            }
        }

        double[] d = Matrix.LUSolve(a, rhs);
        for (int i = 0; i < nnode; i++) {
            ordNM.node[i].value += d[i];
        }

        calculateFixedNodeCurrents(node);

        return iret;
    }



    /*
     * Compute diffusion for time dt and increment the potentials. Ther ordering
     * of points is important (but simple: first the bath, then a depth first
     * tree search, starting anywhere), as done by recIndexPoints <P> This is a
     * sparse matrix calculation without any sort of array to hold the matrix.
     * The right hand side and diagonal elements are stored in the nodes
     * node[i].rhs, node[i].diag, and the off diagonal elements are stored in the
     * link: link.wsA, link.wsB. The elements to the left of the diagonal in row
     * i are stored in node[i].downLinks[*].wsB, those to the right in
     * node[i].upLinks[*].wsA;
     *
     * As the matrix is filled (from the bottom) elements to the right of the
     * diagonal are eliminated. They only involve rows further down which have
     * already been done. This leaves a lower trinagular matrix which then is
     * solved by backsubstitution from the top down.
     *
     * The code is slightly more general than conventional applications - it will
     * allow any number of full rows and columns, as long as they appear before
     * all the rest. Eg, in a single cell context, it is not necessary that the
     * bath be earthed.
     *
     */
    private int sparseNetDiffuse(OrderedNetMap ordNM, double dt) {
        int iret = IOK;

        NetMapNode[] node = ordNM.getNodes();
        node[0].rhs = 0.;
        int nnode = node.length;


        boolean useIntrinsics = ordNM.getUseIntrinsics();

        for (int i = nnode - 1; i >= 0; i--) {

            NetMapNode p = node[i];
            int ndownLink = p.downLink.length;
            int nupLink = p.upLink.length;


            if (p.isFree()) {
                // fill the elements of this row of the Matrix and its rhs;
                p.diag = 0.; // diagonal will contain bath capacitance;
                p.rhs = p.appliedFlux * dt;

                // conductances to nodes earlier in list
                for (int j = 0; j < ndownLink; j++) {
                    NetMapLink c = p.downLink[j];
                    double gdt = dt * c.conductance;
                    p.rhs += gdt * (c.nodeA.value - p.value - c.drive);
                    double cpgt = c.capacitance + gdt;
                    p.diag += cpgt;
                    c.wsB = -cpgt;

                    if (useIntrinsics) {
                        p.rhs += dt * c.current; // POSERR
                    }
                }

                // and to nodes later in list
                for (int j = 0; j < nupLink; j++) {
                    NetMapLink c = p.upLink[j];
                    double gdt = dt * c.conductance;
                    p.rhs += gdt * (c.nodeB.value - p.value + c.drive);
                    double cpgt = c.capacitance + gdt;
                    p.diag += cpgt;
                    c.wsA = -cpgt;

                    if (useIntrinsics) {
                        p.rhs -= dt * c.current; // POSERR
                    }
                }

                if (p.appliedConductance > 0.) {
                    double gdt = dt * p.appliedConductance;
                    p.rhs += gdt * (p.appliedDrive - p.value);
                    p.diag += gdt;
                }


                // eliminate all points to the right of the leading diagonal
                for (int j = nupLink - 1; j >= 0; j--) {
                    NetMapLink c = p.upLink[j];
                    NetMapNode pk = c.nodeB;

                    // using reduced row k to eliminate col k in row i
                    if (pk.diag == 0.) {
                        System.out.println("error - zero diag elt " + c);
                        iret = IERROR;

                    }

                    double ff = c.wsB / pk.diag;

                    // the rhs
                    p.rhs -= ff * pk.rhs;

                    // eliminate to left of diagonal at k; - ignore bath
                    int nkl = pk.downLink.length;
                    for (int m = 0; m < nkl; m++) {
                        p.diag -= ff * pk.downLink[m].wsA;
                    }
                }

            } else {
                // a dummy equation saying 1 * delta_x = 0.
                p.value = p.appliedValue;

                //      E.info("applied fixed val " + p.value);

                p.diag = 1.;
                p.rhs = 0.;

            }
        }

        // backsubstitute
        for (int i = 0; i < nnode; i++) {
            NetMapNode p = node[i];
            int ndownLink = p.downLink.length;
            for (int j = 0; j < ndownLink; j++) {
                p.rhs -= p.downLink[j].wsB * p.downLink[j].nodeA.rhs;
            }
            p.rhs /= p.diag;
            p.value += p.rhs;
        }

        calculateFixedNodeCurrents(node);

        return iret;
    }



    /*
     * NB at present this is calculated only atthe end of the step therfore
     * smaller. Should balance maybe?? ***
     */

    private void calculateFixedNodeCurrents(NetMapNode[] node) {
        for (int i = 0; i < node.length; i++) {
            NetMapNode p = node[i];
            if (!p.isFree()) {
                p.flux = 0.0;
                int ndownLink = p.downLink.length;
                int nupLink = p.upLink.length;

                for (int j = 0; j < ndownLink; j++) {
                    NetMapLink c = p.downLink[j];
                    p.flux -= c.conductance * (c.nodeA.value - p.value - c.drive);

                }
                for (int j = 0; j < nupLink; j++) {
                    NetMapLink c = p.upLink[j];
                    p.flux -= c.conductance * (c.nodeB.value - p.value + c.drive);
                }
            }
        }
    }



}
