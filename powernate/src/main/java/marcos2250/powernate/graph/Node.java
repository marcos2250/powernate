package marcos2250.powernate.graph;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class Node extends RelationalNode {

    private String name;
    private Set<RelationalNode> neighbors;
    private int nodeClassId;

    public Node(String name, int classId) {
        this.name = Preconditions.checkNotNull(name);
        this.nodeClassId = classId;
    }

    public Node(int i) {
        this.name = "Node " + i;
    }

    public String getRelationName() {
        return name;
    }

    public Set<RelationalNode> getNeighboringRelationships() {
        if (neighbors == null) {
            neighbors = Sets.newHashSet();
        }

        return neighbors;
    }

    public boolean addNeighbor(RelationalNode neighbor) {
        Preconditions.checkNotNull(neighbor);
        return getNeighboringRelationships().add(neighbor);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) {
            return false;
        }

        Node that = Node.class.cast(obj);

        return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public String toString() {
        return name;
    }

    @Override
    public int getNodeClass() {
        return nodeClassId;
    }
}
