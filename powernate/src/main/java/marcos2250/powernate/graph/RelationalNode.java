package marcos2250.powernate.graph;

import java.util.Set;

public abstract class RelationalNode {

    private double coordinateX; // = 0.0f;
    private double coordinateY; // = 0.0f;

    public abstract String getRelationName();

    public abstract Set<RelationalNode> getNeighboringRelationships();
    
    public abstract int getNodeClass();

    public double getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(double coordinateX) {
        this.coordinateX = coordinateX;
    }

    public double getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(double coordinateY) {
        this.coordinateY = coordinateY;
    }

}
