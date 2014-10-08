package marcos2250.powernate.graph;

public class Edge {

    private static int edgeCount = 1;

    private double capacity;
    private double weight;
    private int id;

    public Edge() {
        this.id = edgeCount++; // This is defined in the outer class.
        this.weight = 0;
    }

    public Edge(double d, int i) {
        this.id = i;
        this.weight = d;
    }

    public String toString() { // Always good for debugging
        return "E" + id;
    }

    public void incrementWeight() {
        ++this.weight;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getWeight() {
        return weight;
    }

    public int getId() {
        return id;
    }

}
