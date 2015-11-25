package neurord.model;

import java.util.List;

public interface IOutputSet {
    String getIdentifier();
    List<String> getNamesOfOutputSpecies();
    int[] getIndicesOfOutputSpecies(String[] species);
    String getRegion();
    double getOutputInterval(double fallback);
}
