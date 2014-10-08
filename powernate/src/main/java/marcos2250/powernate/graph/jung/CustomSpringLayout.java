//CHECKSTYLE:OFF
package marcos2250.powernate.graph.jung;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;
import org.apache.commons.collections15.functors.CloneTransformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.map.LazyMap;

import marcos2250.powernate.graph.RelationalNode;

@SuppressWarnings("all")
public class CustomSpringLayout<V extends RelationalNode, E> {

    private Set<V> dontmove = new HashSet<V>();

    private Dimension size;
    private Graph<V, E> graph;
    private boolean initialized;

    private int currentIteration;
    private int averageCounter;
    private int loopCountMax = 4;
    private boolean done;

    private Point2D averageDelta = new Point2D.Double();

    private double stretch = 0.70;
    private Transformer<E, Integer> lengthFunction;

    private int repulsionRangeSq = 100 * 100;
    private double forceMultiplier = 1.0 / 3.0;

    private Map<V, SpringVertexData> springVertexData = LazyMap.decorate(new HashMap<V, SpringVertexData>(),
            new Factory<SpringVertexData>() {
                public SpringVertexData create() {
                    return new SpringVertexData();
                }
            });

    private Map<V, Point2D> locations = LazyMap.decorate(new HashMap<V, Point2D>(), new Transformer<V, Point2D>() {
        public Point2D transform(V arg0) {
            return new Point2D.Double();
        }
    });

    /**
     * Creates an instance which does not initialize the vertex locations.
     * 
     * @param graph the graph for which the layout algorithm is to be created.
     */
    public CustomSpringLayout(Graph<V, E> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph must be non-null");
        }
        this.graph = graph;
        this.lengthFunction = new ConstantTransformer(30);
    }

    public void setGraph(Graph<V, E> graph) {
        this.graph = graph;
        if (size != null && graph != null) {
            initialize();
        }
    }

    private void adjustLocations(Dimension oldSize, Dimension size) {

        int xOffset = (size.width - oldSize.width) / 2;
        int yOffset = (size.height - oldSize.height) / 2;

        // now, move each vertex to be at the new screen center
        while (true) {
            try {
                for (V v : getGraph().getVertices()) {
                    offsetVertex(v, xOffset, yOffset);
                }
                break;
            } catch (ConcurrentModificationException cme) {
                cme.printStackTrace();
            }
        }
    }

    public boolean isLocked(V v) {
        return dontmove.contains(v);
    }

    @SuppressWarnings("unchecked")
    public void setInitializer(Transformer<V, Point2D> initializer) {
        if (this.equals(initializer)) {
            throw new IllegalArgumentException("Layout cannot be initialized with itself");
        }
        Transformer<V, ? extends Object> chain = ChainedTransformer.getInstance(initializer,
                CloneTransformer.getInstance());
        this.locations = LazyMap.decorate(new HashMap<V, Point2D>(), (Transformer<V, Point2D>) chain);
        initialized = true;
    }

    /**
     * Returns the current size of the visualization space, accoring to the last call to resize().
     * 
     * @return the current size of the screen
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * Returns the Coordinates object that stores the vertex' x and y location.
     * 
     * @param v A Vertex that is a part of the Graph being visualized.
     * @return A Coordinates object with x and y locations.
     */
    private Point2D getCoordinates(V v) {
        return locations.get(v);
    }

    public Point2D transform(V v) {
        return getCoordinates(v);
    }

    /**
     * Returns the x coordinate of the vertex from the Coordinates object. in most cases you will be better off calling
     * transform(v).
     */
    public double getX(V v) {
        assert getCoordinates(v) != null : "Cannot getX for an unmapped vertex " + v;
        return getCoordinates(v).getX();
    }

    /**
     * Returns the y coordinate of the vertex from the Coordinates object. In most cases you will be better off calling
     * transform(v).
     */
    public double getY(V v) {
        assert getCoordinates(v) != null : "Cannot getY for an unmapped vertex " + v;
        return getCoordinates(v).getY();
    }

    /**
     * @param v
     * @param xOffset
     * @param yOffset
     */
    private void offsetVertex(V v, double xOffset, double yOffset) {
        Point2D c = getCoordinates(v);
        c.setLocation(c.getX() + xOffset, c.getY() + yOffset);
        setLocation(v, c);
    }

    /**
     * Accessor for the graph that represets all vertices.
     * 
     * @return the graph that contains all vertices.
     */
    public Graph<V, E> getGraph() {
        return graph;
    }

    /**
     * Forcibly moves a vertex to the (x,y) location by setting its x and y locations to the inputted location. Does
     * not add the vertex to the "dontmove" list, and (in the default implementation) does not make any adjustments to
     * the rest of the graph.
     */
    public void setLocation(V picked, double x, double y) {
        Point2D coord = getCoordinates(picked);
        coord.setLocation(x, y);
    }

    public void setLocation(V picked, Point2D p) {
        Point2D coord = getCoordinates(picked);
        coord.setLocation(p);
    }

    /**
     * Locks {@code v} in place if {@code state} is {@code true}, otherwise unlocks it.
     */
    public void lock(V v, boolean state) {
        if (state) {
            dontmove.add(v);
        } else {
            dontmove.remove(v);
        }
    }

    /**
     * Locks all vertices in place if {@code lock} is {@code true}, otherwise unlocks all vertices.
     */
    public void lock(boolean lock) {
        for (V v : graph.getVertices()) {
            lock(v, lock);
        }
    }

    /**
     * Returns the current value for the stretch parameter.
     * 
     * @see #setStretch(double)
     */
    public double getStretch() {
        return stretch;
    }

    /**
     * Sets the dimensions of the available space for layout to {@code size}.
     */
    public void setSize(Dimension size) {
        if (!initialized) {
            setInitializer(new RandomLocationTransformer<V>(size));
        }

        if (size != null && graph != null) {

            Dimension oldSize = this.size;
            this.size = size;
            initialize();

            if (oldSize != null) {
                adjustLocations(oldSize, size);
            }
        }
    }

    /**
     * <p>
     * Sets the stretch parameter for this instance. This value specifies how much the degrees of an edge's incident
     * vertices should influence how easily the endpoints of that edge can move (that is, that edge's tendency to
     * change its length).
     * </p>
     * 
     * <p>
     * The default value is 0.70. Positive values less than 1 cause high-degree vertices to move less than low-degree
     * vertices, and values > 1 cause high-degree vertices to move more than low-degree vertices. Negative values will
     * have unpredictable and inconsistent results.
     * </p>
     * 
     * @param stretch
     */
    public void setStretch(double stretch) {
        this.stretch = stretch;
    }

    /**
     * Returns the current value for the node repulsion range.
     * 
     * @see #setRepulsionRange(int)
     */
    public int getRepulsionRange() {
        return (int) (Math.sqrt(repulsionRangeSq));
    }

    /**
     * Sets the node repulsion range (in drawing area units) for this instance. Outside this range, nodes do not repel
     * each other. The default value is 100. Negative values are treated as their positive equivalents.
     * 
     * @param range
     */
    public void setRepulsionRange(int range) {
        this.repulsionRangeSq = range * range;
    }

    /**
     * Returns the current value for the edge length force multiplier.
     * 
     * @see #setForceMultiplier(double)
     */
    public double getForceMultiplier() {
        return forceMultiplier;
    }

    /**
     * Sets the force multiplier for this instance. This value is used to specify how strongly an edge "wants" to be
     * its default length (higher values indicate a greater attraction for the default length), which affects how much
     * its endpoints move at each timestep. The default value is 1/3. A value of 0 turns off any attempt by the layout
     * to cause edges to conform to the default length. Negative values cause long edges to get longer and short edges
     * to get shorter; use at your own risk.
     */
    public void setForceMultiplier(double force) {
        this.forceMultiplier = force;
    }

    public void initialize() {
        // ntd
    }

    private void relaxEdges() {
        try {
            for (E e : getGraph().getEdges()) {
                Pair<V> endpoints = getGraph().getEndpoints(e);
                V v1 = endpoints.getFirst();
                V v2 = endpoints.getSecond();

                Point2D p1 = transform(v1);
                Point2D p2 = transform(v2);
                if (p1 == null || p2 == null) {
                    continue;
                }

                double vx = p1.getX() - p2.getX();
                double vy = p1.getY() - p2.getY();
                double len = Math.sqrt(vx * vx + vy * vy);

                double desiredLen = lengthFunction.transform(e);

                // round from zero, if needed [zero would be Bad.].
                len = (len == 0) ? .0001 : len;

                int v1Class = v1.getNodeClass();
                int v2Class = v1.getNodeClass();

                double forceMultiplier = this.forceMultiplier;
                double stretch = this.stretch;

                if (v1Class == v2Class) {
                    // forceMultiplier *= 2;
                    // stretch *= 2;
                }

                double f = forceMultiplier * (desiredLen - len) / len;

                f = f * Math.pow(stretch, (getGraph().degree(v1) + getGraph().degree(v2) - 2));

                // the actual movement distance 'dx' is the force multiplied by the
                // distance to go.
                double dx = f * vx;
                double dy = f * vy;
                SpringVertexData v1D, v2D;
                v1D = springVertexData.get(v1);
                v2D = springVertexData.get(v2);

                v1D.edgedx += dx;
                v1D.edgedy += dy;
                v2D.edgedx += -dx;
                v2D.edgedy += -dy;
            }
        } catch (ConcurrentModificationException cme) {
            relaxEdges();
        }
    }

    private void calculateRepulsion() {
        try {
            for (V v : getGraph().getVertices()) {
                if (isLocked(v)) {
                    continue;
                }

                SpringVertexData svd = springVertexData.get(v);
                if (svd == null) {
                    continue;
                }
                double dx = 0, dy = 0;

                for (V v2 : getGraph().getVertices()) {
                    if (v == v2) {
                        continue;
                    }
                    Point2D p = transform(v);
                    Point2D p2 = transform(v2);
                    if (p == null || p2 == null) {
                        continue;
                    }
                    double vx = p.getX() - p2.getX();
                    double vy = p.getY() - p2.getY();
                    double distanceSq = p.distanceSq(p2);

                    int repulsionRangeSq = this.repulsionRangeSq;

                    if (v.getNodeClass() != v2.getNodeClass()) {
                        repulsionRangeSq *= 10;
                    }

                    if (distanceSq == 0) {
                        dx += Math.random();
                        dy += Math.random();
                    } else if (distanceSq < repulsionRangeSq) {

                        double factor = 1;
                        if (v.getNodeClass() != v2.getNodeClass()) {
                            factor *= 2;
                        }

                        dx += factor * vx / distanceSq;
                        dy += factor * vy / distanceSq;
                    }
                }
                double dlen = dx * dx + dy * dy;
                if (dlen > 0) {
                    dlen = Math.sqrt(dlen) / 2;
                    svd.repulsiondx += dx / dlen;
                    svd.repulsiondy += dy / dlen;
                }
            }
        } catch (ConcurrentModificationException cme) {
            calculateRepulsion();
        }
    }

    private static class SpringVertexData {
        private double edgedx;
        private double edgedy;
        private double repulsiondx;
        private double repulsiondy;

        /** movement speed, x */
        private double dx;

        /** movement speed, y */
        private double dy;
    }

    /**
     * Used for changing the size of the layout in response to a component's size.
     */
    public class SpringDimensionChecker extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            setSize(e.getComponent().getSize());
        }
    }

    /**
     * This one is an incremental visualization
     */
    public boolean isIncremental() {
        return true;
    }

    /**
     * No effect.
     */
    public void reset() {
        // ntd
    }

    public boolean isDone() {
        return done;
    }

    /**
     * Relaxation step. Moves all nodes a smidge.
     */
    public void step() {
        try {
            for (V v : getGraph().getVertices()) {
                SpringVertexData svd = springVertexData.get(v);
                if (svd == null) {
                    continue;
                }
                svd.dx /= 4;
                svd.dy /= 4;
                svd.edgedx = svd.edgedy = 0;
                svd.repulsiondx = svd.repulsiondy = 0;
            }
        } catch (ConcurrentModificationException cme) {
            step();
        }

        relaxEdges();
        calculateRepulsion();
        moveNodes();

        currentIteration++;
        testAverageDeltas();
    }

    private void testAverageDeltas() {
        double dx = this.averageDelta.getX();
        double dy = this.averageDelta.getY();
        if (Math.abs(dx) < .001 && Math.abs(dy) < .001) {
            done = true;
            System.err.println("done, dx=" + dx + ", dy=" + dy);
        }
        if (currentIteration > loopCountMax) {
            this.averageDelta.setLocation(0, 0);
            averageCounter = 0;
            currentIteration = 0;
        }
    }

    private void moveNodes() {
        synchronized (getSize()) {
            try {
                for (V v : getGraph().getVertices()) {
                    if (isLocked(v)) {
                        continue;
                    }
                    SpringVertexData vd = springVertexData.get(v);
                    if (vd == null) {
                        continue;
                    }
                    Point2D xyd = transform(v);

                    vd.dx += vd.repulsiondx + vd.edgedx;
                    vd.dy += vd.repulsiondy + vd.edgedy;

                    averageDelta.setLocation(((averageDelta.getX() * averageCounter) + vd.dx) / (averageCounter + 1),
                            ((averageDelta.getY() * averageCounter) + vd.dy) / (averageCounter + 1));
                    averageCounter++;

                    // keeps nodes from moving any faster than 5 per time unit
                    xyd.setLocation(xyd.getX() + Math.max(-5, Math.min(5, vd.dx)),
                            xyd.getY() + Math.max(-5, Math.min(5, vd.dy)));

                    Dimension d = getSize();
                    int width = d.width;
                    int height = d.height;

                    if (xyd.getX() < 0) {
                        xyd.setLocation(0, xyd.getY());
                    } else if (xyd.getX() > width) {
                        xyd.setLocation(width, xyd.getY()); // setX(width);
                    }
                    if (xyd.getY() < 0) {
                        xyd.setLocation(xyd.getX(), 0);
                    } else if (xyd.getY() > height) {
                        xyd.setLocation(xyd.getX(), height); // setY(height);
                    }

                }
            } catch (ConcurrentModificationException cme) {
                moveNodes();
            }
        }
    }

    private class RandomLocationTransformer<V> implements Transformer<V, Point2D> {

        private Dimension d;
        private Random random;

        public RandomLocationTransformer(Dimension d) {
            this(d, new Date().getTime());
        }

        public RandomLocationTransformer(final Dimension d, long seed) {
            this.d = d;
            this.random = new Random(seed);
        }

        public Point2D transform(V v) {
            return new Point2D.Double(random.nextDouble() * d.width, random.nextDouble() * d.height);
        }
    }

}