package marcos2250.powernate.graph;

import java.awt.Dimension;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import marcos2250.powernate.graph.jung.CustomSpringLayout;
import marcos2250.powernate.graph.jung.Graph;

/**
 * Classe principal - Gerador de grafos.
 */
@SuppressWarnings("PMD.MagicNumbers")
public class GraphGenerator {

    public static final int Y_AXIS_HEIGHT = 1360;
    public static final int X_AXIS_WIDTH = 1000;

    private Set<Node> nodes;

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerator.class);

    public GraphGenerator() {
        nodes = Sets.newHashSet();
    }

    public void build() {

        LOGGER.info("Gerador de grafos - Reconstruindo modelo entidade relacional...");

        Graph<RelationalNode, Edge> network = GraphConstruction.build(nodes);
        CustomSpringLayout<RelationalNode, Edge> layout = new CustomSpringLayout<RelationalNode, Edge>(network);

        layout.setSize(new Dimension(X_AXIS_WIDTH, Y_AXIS_HEIGHT));

        int centerX = X_AXIS_WIDTH / 2;
        int centerY = Y_AXIS_HEIGHT / 2;
        for (RelationalNode node : network.getVertices()) {
            layout.setLocation(node, centerX, centerY);
        }

        layout.initialize();

        // 1st pass
        layout.setStretch(1);
        layout.setForceMultiplier(0.5);
        layout.setRepulsionRange(300);
        for (int i = 0; i < 2000; i++) {
            layout.step();
        }

        // 2nd pass
        layout.setStretch(0.9);
        layout.setForceMultiplier(0.2);
        layout.setRepulsionRange(50);
        for (int i = 0; i < 1000; i++) {
            layout.step();
        }

        for (RelationalNode node : network.getVertices()) {
            node.setCoordinateX(layout.getX(node));
            node.setCoordinateY(layout.getY(node));
        }

        LOGGER.info("Gerador de grafos - Concluido!");

    }

    public Node addNode(String name, int nodeClassId) {

        LOGGER.trace("Inserindo o vertice " + name + "(classe = " + nodeClassId + ") na rede...");

        Node existentNode = getNode(name);
        if (existentNode != null) {
            LOGGER.trace("Ja existe o vertice " + name + " da classe " + existentNode.getNodeClass());
            if (existentNode.getNodeClass() != nodeClassId) {
                LOGGER.trace("A primeira classe do vertice " + name + " era " + existentNode.getNodeClass()
                        + ", mas houve a tentativa de troca-la para " + nodeClassId);
            }

            return existentNode;
        }

        Node node = new Node(name, nodeClassId);
        nodes.add(node);
        return node;
    }

    public Set<Node> getAllNodes() {
        return this.nodes;
    }

    private Node getNode(String name) {
        Preconditions.checkNotNull(name, "Nao e possivel buscar um no com nome NULO!");
        for (Node node : nodes) {
            if (node.getRelationName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    private void addNeighbor(Node nodeA, Node nodeB) {
        if (nodeA.getNeighboringRelationships().contains(nodeB) || nodeB.getNeighboringRelationships().contains(nodeA)) {
            return;
        }
        nodeA.addNeighbor(nodeB);
    }

    public void addNeighbor(String nodeAName, String nodeBName) {

        Node nodeA = getNode(nodeAName);

        if (nodeA == null) {
            return;
        }
        Preconditions.checkNotNull(nodeA, "E necessario inserir o no " + nodeAName
                + " antes de criar uma ligacao com o no " + nodeBName);

        Node nodeB = getNode(nodeBName);
        if (nodeB == null) {
            return;
        }

        Preconditions.checkNotNull(nodeB, "E necessario inserir o no  " + nodeBName
                + " antes de criar uma ligacao com o no  " + nodeAName);

        addNeighbor(nodeA, nodeB);

    }

}
