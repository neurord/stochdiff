package org.catacomb.numeric.phys;




/*

 Calculation units for cells:

   ms            milliseconds
   mV            millivolts
   pA            picoamps
   fC            femtocoulombs
   pF            picofarads
   nS            nanosiemens
   GOhm          gigaohms
   micron        micrometers
   M             Molar
   GOhm micron       for resistivity


   eg  1 pA flowing onto a 1 pF capacitor raises the potential
       by 1 mV per ms

       1 pA flowing through a resistance of 1 GOhm gives a potential drop
       of 1mV

       1 mV across a conductance of 1 nS gives a current of 1 pA

       1 fC per ms is 1pC per second, or 1 pA


   So, to get capacitance from area (micron^2) and C_mem in muF/cm^2
   need x 10^6 for micro to pico and / 10^8 for cm to micron twice.
   ie, 1.e-2;

   1/resistance in MOhm gives conductance in microS - need to multiply
   conductances by 1000.


   To convert from Ohm cm to GOhm micron, mply by 10-9 * 10^4 = 10^-5




   ##### everything under the calc package is in these units #####
   other catacomb packages can use whatever units they like,
   but must call the appropriate conversion when exporting to
   objects from calc.


   The idea of this class is that components needn't know what
   units to export in - they just call the appropriate converter,
   saying what units thay have.

 */

public final class Units {
    public final static double from_ms(double x) {
        return x;
    }
    public final static double from_nm(double x) {
        return 1.e-3 * x;
    }
    public final static double from_micron(double x) {
        return x;
    }
    public final static double from_micron2(double x) {
        return x;
    }
    public final static double from_mV(double x) {
        return x;
    }
    public final static double from_V(double x) {
        return x * 1000.;
    }
    public final static double from_perV(double x) {
        return x * 1e-3;
    }
    public final static double from_Ohmcm(double x) {
        return 1e-5 * x;
    }
    public final static double from_microFpercm2(double x) {
        return 1.e-2 *x;
    }
    public final static double from_pS(double x) {
        return 1.e-3 * x;
    }
    public final static double from_nS(double x) {
        return x;
    }
    public final static double from_microS(double x) {
        return 1.e3 * x;
    }
    public final static double from_pA(double x) {
        return x;
    }
    public final static double from_nA(double x) {
        return 1.e3 * x;
    }
    public final static double from_A(double x) {
        return 1.e-12 * x;
    }
    public final static double from_Celcius(double x) {
        return 273 + x;
    }
    public final static double from_electrons(double x) {
        return 1.e15 * Phys.electronCharge * x;
    }
    public final static double from_degrees(double x) {
        return 2. * Math.PI * x / 360.;
    }
}




