package org.textensor.stochdiff.reduce;

import java.io.File;
import java.util.ArrayList;

import org.textensor.util.FileUtil;
import org.textensor.report.E;
import org.textensor.stochdiff.inter.FloatValued;
import org.textensor.stochdiff.inter.SDState;
import org.textensor.stochdiff.model.InitialConditions;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.model.SDRunWrapper;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.StaticCalc;
import org.textensor.stochdiff.numeric.math.Matrix;
import org.textensor.xml.ModelReader;

public class Reducer {

    // the base model specification
    SDRun sdr;

    // the target state to be fitted
    SDState sds;

    // static calc is like SteppedStochasticGridCalc except without the calculation element:
    // it just reads and processes the inputs
    StaticCalc staticCalc;

    // initial conditions element extracted from SDRun
    InitialConditions icon;

    // the accessible quantities in the initial conditions specification
    ArrayList<FloatValued> afv;

    // various quantities derived from the above
    // number of free variables (length of afv)
    int nv;

    // volumes of each element in the grid
    double[] vols;

    // number of elements
    int nel;

    // number of species
    int nspec;

    // the target concentrations from the target state, nel * nspec
    double[][] ctgt;

    public final ModelReader<InitialConditions> ic_loader
        = new ModelReader(InitialConditions.class);

    public Reducer(SDRun sdModel, SDState sdState) {
        sdr = sdModel;
        sds = sdState;
    }


    public void reduce()
        throws Exception
    {
        E.info("Starting reduce");
        icon = sdr.getInitialConditions();
        afv = icon.getFloatValuedElements();
        nv = afv.size();

        // these don't actually matter as it is all linear
        // all concentrations are set to x0 and then they are separately varied by dx to
        // compute the matrix of derivatives of the concentrations with respect to the accessible variables
        double x0 = 10.0;
        double dx = 0.1;

        E.info("The template provides " + nv + " accessible concentration values");

        // StaticCalc maps the model onto the array of concentrations for each element
        SDRunWrapper wrapper = new SDRunWrapper(sdr);
        staticCalc = new StaticCalc(0, wrapper);
        staticCalc.init();
        vols = staticCalc.getVolumes();
        nspec = staticCalc.getNSpec();
        nel = staticCalc.getNel();


        // the target concentrations from the SDState object
        ctgt = sds.getConc2();

        // c0 is the array of unperturbed concentrations for all elements and species
        for (int j = 0; j < nv; j++) {
            afv.get(j).setValue(x0);
        }
        double[] c0 = staticCalc.getConcentrations();


        int nconc = c0.length;
        E.info("The template provides " + nv + " variable quantities");
        E.info("After discretization there are " + nconc + " state variables arising from " + nspec + " species in "
               + nel + " elements");

        // bm will contain the derivatives of the concentrations with respect to the variables
        double[][] bm = new double[nconc][nv];

        for (int i = 0; i < nv; i++) {
            // set all initial values to x0 except for variable i which is set to x0 + dx
            for (int j = 0; j < nv; j++) {
                afv.get(j).setValue(x0);
            }
            afv.get(i).setValue(x0 + dx);
            double[] cwk = staticCalc.getConcentrations();

            // numerical derivatives go in bm
            for (int k = 0; k < nconc; k++) {
                bm[k][i] = (cwk[k] - c0[k]) / dx;
            }
        }



        // if N variables give rise to M measures, (N << M) then we have
        // mes(1,...M) = b * var(1,...,N);
        // and
        // bT mes = bT * b * var
        // where bT is the transpose of b
        // bT * b is square and invertible so
        // var = inverse(bT * b) * bT * mes

        // tgtconc is like ctgt, but flattened into a single array
        double[] tgtconc = sds.getConc1();


        // evaluate var = inverst(bT * b) * bT * mes
        Matrix b = new Matrix(bm);
        Matrix bt = b.transpose();
        Matrix mtgtconc = new Matrix(Matrix.COLUMN, tgtconc);
        Matrix btb = bt.times(b);
        Matrix btbi = btb.inverse();
        Matrix res = btbi.times(bt.times(mtgtconc));

        // res is a matrix. newvars is just a plain array of the contents
        double[] fit = res.getColumn(0);


        // if there are any constraints in the initial conditions file, then modify the fit
        // accordingly
        String[] spres = icon.getTotalPreserved();
        if (spres.length > 0) {

            // number of constraints
            int ncn = spres.length;

            int[] ispeccon = staticCalc.getSpecieIndexes(spres);
            String ss = "";
            for (int i = 0; i < ncn; i++) {
                ss += " " + spres[i] + "(" + ispeccon[i] + ") ";
            }
            E.info(" " + ncn + " constraint(s) on totals for species " + ss);

            // q is the constraint matrix
            double[][] q = new double[ncn][nv];

            // qtgt contains the values for each constraint (desired result of q * fit)
            double[] qtgt = new double[ncn];

            for (int icn = 0; icn < ncn; icn++) {
                int ispec = ispeccon[icn];
                for (int iel = 0; iel < nel; iel++) {
                    for (int i = 0; i < nv; i++) {
                        q[icn][i] += vols[iel] * bm[iel * nspec + ispec][i];
                    }
                    qtgt[icn] += vols[iel] * ctgt[iel][ispec];
                }
            }

            // The matrix calculation is similar in spirit to the above expression for the fit,
            // but a little more complicated. c is the target value of the constraints (qtgt above)
            // fit_constrained = fit - (BtB)inv Q (Qt (BtB)inv Q)inv (Qt fit - c)
            Matrix mq = new Matrix(q);
            Matrix mr = new Matrix(Matrix.COLUMN, fit);
            Matrix mc = new Matrix(Matrix.COLUMN, qtgt);

            Matrix wk1 = mq.times(mr).subtract(mc);
            Matrix wk2 = mq.times(btbi).times(mq.transpose());
            Matrix wk3 = mq.transpose().times(wk2.inverse());
            Matrix wk4 = btbi.times(wk3).times(wk1);
            Matrix mrcon = mr.subtract(wk4);

            fit = mrcon.getColumnData();
        }


        // display the resulting fit.
        // The second argument contains the concentrations that come from b * fit instead of
        // via the StaticCalc element. These should be the same except where there are
        // nonlinearities in the forward process as occur if some of the initial concentrations are
        // negative.
        showFit(fit, b.times(new Matrix(Matrix.COLUMN, fit)).getColumnData());



        for (int j = 0; j < nv; j++) {
            afv.get(j).setValue(fit[j]);
        }
        String fout = "reduce-fit.xml";
        ic_loader.marshall(icon, fout);
        E.info("New initial conditions have been written to " + fout);
    }

    public void showFit(double[] newvars, double[] mcdat) {
        for (int j = 0; j < nv; j++) {
            afv.get(j).setValue(newvars[j]);
        }

        double[][] cfit = staticCalc.getElementConcentrations();

        E.info("Average concentrations by species: ");
        for (int ispec = 0; ispec < nspec; ispec++) {
            double a = 0;
            double b = 0;
            double c = 0;
            double vtot = 0;
            for (int iel = 0; iel < nel; iel++) {
                a += vols[iel] * ctgt[iel][ispec];
                b += vols[iel] * cfit[iel][ispec];
                c += vols[iel] * mcdat[iel * nspec + ispec];
                vtot += vols[iel];
            }
            a /= vtot;
            b /= vtot;
            E.info("    species " + ispec + " target=" + a + "  fit=" + b);

        }




    }


    public void printconc(double[][] dat) {

        for (int i = 0; i < nel; i++) {
            String sl = "elt " + i + " ";
            for (int j = 0; j < nspec; j++) {
                sl += String.format("%12.3f", dat[i][j]) + " ";
            }
            E.info(sl);
        }
    }
}
