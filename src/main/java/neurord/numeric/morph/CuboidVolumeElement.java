package neurord.numeric.morph;

import neurord.geom.Position;

public class CuboidVolumeElement extends VolumeElement {
    public CuboidVolumeElement(String label, String region, String groupID,
                               Position[] boundary,
                               Position[] surfaceBoundary,
                               double exposedArea,
                               double alongArea, double sideArea, double topArea,
                               double volume, double deltaZ) {

        super(label, region, groupID,
              boundary,
              surfaceBoundary,
              exposedArea,
              alongArea, sideArea, topArea,
              volume, deltaZ);
    }
}
