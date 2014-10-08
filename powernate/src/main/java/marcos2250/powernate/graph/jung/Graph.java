package marcos2250.powernate.graph.jung;

import java.util.Collection;

import marcos2250.powernate.graph.RelationalNode;

public interface Graph<V extends RelationalNode, E> {

    Collection<E> getInEdges(V vertex);

    Collection<E> getOutEdges(V vertex);

    Collection<V> getPredecessors(V vertex);

    Collection<V> getSuccessors(V vertex);

    int inDegree(V vertex);

    int outDegree(V vertex);

    boolean isPredecessor(V v1, V v2);

    boolean isSuccessor(V v1, V v2);

    int getPredecessorCount(V vertex);

    int getSuccessorCount(V vertex);

    V getSource(E directedEdge);

    V getDest(E directedEdge);

    boolean isSource(V vertex, E edge);

    boolean isDest(V vertex, E edge);

    boolean addEdge(E e, V v1, V v2);

    boolean addEdge(E e, V v1, V v2, EdgeType edgeType);

    Pair<V> getEndpoints(E edge);

    V getOpposite(V vertex, E edge);

    Collection<E> getEdges();

    Collection<V> getVertices();

    boolean containsVertex(V vertex);

    boolean containsEdge(E edge);

    int getEdgeCount();

    int getVertexCount();

    Collection<V> getNeighbors(V vertex);

    Collection<E> getIncidentEdges(V vertex);

    Collection<V> getIncidentVertices(E edge);

    E findEdge(V v1, V v2);

    Collection<E> findEdgeSet(V v1, V v2);

    boolean addVertex(V vertex);

    boolean addEdge(E edge, Collection<? extends V> vertices);

    boolean addEdge(E edge, Collection<? extends V> vertices, EdgeType edgeType);

    boolean removeVertex(V vertex);

    boolean removeEdge(E edge);

    boolean isNeighbor(V v1, V v2);

    boolean isIncident(V vertex, E edge);

    int degree(V vertex);

    int getNeighborCount(V vertex);

    int getIncidentCount(E edge);

    EdgeType getEdgeType(E edge);

    EdgeType getDefaultEdgeType();

    Collection<E> getEdges(EdgeType edgeType);

    int getEdgeCount(EdgeType edgeType);

}