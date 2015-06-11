package org.textensor.stochdiff.model;

import java.util.List;

public interface IOutputSet {
    List<String> getNamesOfOutputSpecies();
    int[] getIndicesOfOutputSpecies(String[] species);
    String getRegion();
    double getOutputInterval(double fallback);
}
