package neurord.numeric.morph;

import neurord.geom.Position;


public class CuboidVolumeElement extends VolumeElement {
    public CuboidVolumeElement(String label, String region, String groupID) {
        super(label, region, groupID, 0.0, 0.0, 0.0);
    }

    public CuboidVolumeElement(String label, String region, String groupID,
                               double alongArea, double sideArea, double topArea) {
        super(label, region, groupID, alongArea, sideArea, topArea);
    }
}
