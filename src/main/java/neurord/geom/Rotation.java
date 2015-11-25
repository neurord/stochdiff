package neurord.geom;


public interface Rotation {

    public Vector getRotatedVector(Vector v);

    public Position getRotatedPosition(Position p);

    public Position getRotatedPosition(Position p, Position pcenter);

    public void rotateAbout(Movable mov, Position pcenter);

}
