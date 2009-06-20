package org.catacomb.numeric.function;


public class FixedValueScalarFunction implements ScalarFunction {

    double value;

    public FixedValueScalarFunction(double d) {
        value = d;
    }


    public double getScalar(double t) {
        return value;
    }


}
