package marcos2250.powernate.graph;

import java.util.Set;

import marcos2250.powernate.graph.jung.Graph;
import marcos2250.powernate.graph.jung.SparseGraph;

public class GraphConstruction {

    private GraphConstruction() {
        // service class
    }

    public static Graph<RelationalNode, Edge> build(Set<? extends RelationalNode> relationalNodes) {

        Graph<RelationalNode, Edge> network = new SparseGraph<RelationalNode, Edge>();

        for (RelationalNode referenceNode : relationalNodes) {

            network.addVertex(referenceNode);

            for (RelationalNode neighbor : referenceNode.getNeighboringRelationships()) {

                if (!network.containsVertex(neighbor)) {
                    network.addVertex(neighbor);
                }

                Edge edge = network.findEdge(referenceNode, neighbor);
                if (edge == null) {
                    edge = new Edge();
                }

                edge.incrementWeight();

                network.addEdge(edge, referenceNode, neighbor);
            }

        }

        return network;

    }

}
