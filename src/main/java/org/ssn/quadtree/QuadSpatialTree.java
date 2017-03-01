package org.ssn.quadtree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Nechkin
 */
public class QuadSpatialTree {
    private Node root;
    private double minDimensionSize;
    private boolean inheritPolygons = true;

    public QuadSpatialTree() {
        // QuadSpatialTree has equally sized bins but for variable area sizes.
        // For lat 0.00898 grad ~ 1 km.
        // 0.0055 ~ 0.61 km.
        // minDimensionSize = 0.005, hence ~0.0055 will be the smallest allowed bin width.
        // Tree depth will be 16 (due to max lon being 360).
        // For lon on equator 0.0055 ~ 0.61 km, diminishing towards the poles.
        this(-180.000000, -90.000000, 180.000000, 90.000000, 0.005);
//        this(-180.000000, -90.000000, 180.000000, 90.000000, 0.5);
    }

    public QuadSpatialTree(double minX, double minY, double maxX, double maxY,
                           double minDimensionSize) {
        final double halfW = (maxX - minX) / 2.0;
        final double halfH = (maxY - minY) / 2.0;
        root = new Node(minX + halfW, minY + halfH, halfW, halfH, null);
        this.minDimensionSize = minDimensionSize;
    }

    public void insert(final List<Point> poly, final Object value) {
        final List<Point> loopedPoly = new ArrayList<>(poly);
        loopedPoly.add(poly.get(0));
        insert(root, loopedPoly, value);
    }

    private boolean insert(final Node parent, final List<Point> poly, final Object value) {
        // -2 A intersects Node
        // -1 A inside Node
        // 0 A outside of Node
        // 1 A contains Node
        final int collisionType = parent.collisionType(poly);
        if (collisionType < 0) {
            // poly intersects or is inside the parent, split, try insert for quadrants
            if (parent.isLeaf()) {
                // Parent is not split, no region was assigned before
                boolean regionInserted = false;
                if (split(parent)) {
                    regionInserted = insert(parent.getNw(), poly, value);
                    regionInserted |= insert(parent.getNe(), poly, value);
                    regionInserted |= insert(parent.getSw(), poly, value);
                    regionInserted |= insert(parent.getSe(), poly, value);
                }
                if (!regionInserted) {
                    // Remove nodes if no region was assigned to them
                    parent.setNw(null);
                    parent.setNe(null);
                    parent.setSw(null);
                    parent.setSe(null);
                }
                return regionInserted;
            } else {
                // Parent was split before, some region is assigned to one of it's children
                insert(parent.getNw(), poly, value);
                insert(parent.getNe(), poly, value);
                insert(parent.getSw(), poly, value);
                insert(parent.getSe(), poly, value);
                return true;
            }
        } else if (collisionType == 1) {
            // poly contains parent, assign poly to this parent
            List<Object> pv = parent.getPolygonValues();
            if (pv == null) {
                pv = new ArrayList<>();
                parent.setPolygonValues(pv);
            }
            pv.add(value);
            if (inheritPolygons) {
                parent.inheritPolygons();
            }
            return true;
        } else {
            // poly is outside, do nothing
            return false;
        }
    }

    private boolean split(Node node) {
        double qw = node.getHalfW() / 2.0; // quarter of the node's size
        double qh = node.getHalfH() / 2.0;

        // Check if can divide at least for one dimension
        boolean splitAllowed = true;
        if (qw < minDimensionSize) {
            qw = node.getHalfW();
            splitAllowed = false;
        }
        if (qh < minDimensionSize) {
            qh = node.getHalfH();
            splitAllowed = false;
        }
        if (!splitAllowed) return false;

        double xc = node.getXc();
        double yc = node.getYc();

        node.setNw(new Node(xc - qw, yc + qh, qw, qh, node));
        node.setNe(new Node(xc + qw, yc + qh, qw, qh, node));
        node.setSw(new Node(xc - qw, yc - qh, qw, qh, node));
        node.setSe(new Node(xc + qw, yc - qh, qw, qh, node));

        return true;
    }

    /**
     * Find all Polygons/regions inside which point (lon, lat) resides.
     * Returns a list of Nodes assigned to these polygons.
     * The first node in the returned list is the closest to the tree's root among the found nodes.
     * @param node  start search from this node, doesn't check
     */
    public List<Node> searchFromNode(final Node node, final double lon, final double lat) {
        List<Node> result = new ArrayList<Node>();
        if (node.containsPoint(lon, lat)) {
            // Specified node contains the point
            navigate0(result, node, lon, lat);
        } else {
            // Try parent
            if (node.getParent() != null) return searchFromNode(node.getParent(), lon, lat);
        }
        return result;
    }

    // node is supposed to contain the point
    private void navigate0(final List<Node> result, final Node node, final double lon, final double lat) {
        if (node.getPolygonValues() != null) {
            // PolygonValues are not null if the node is completely inside the region
            result.add(node); //result.addAll(node.getPolygonValues());
        }
        if (!node.isLeaf()) {
            // If there are children, they can also have regions assigned to them
            // Can happen if we have intersecting regions (probably a rare case)
            navigate0(result, getQuadrantForPoint(node, lon, lat), lon, lat);
        }
    }

    /**
     * Intentionally return only a single Node for simplicity
     * i.e. in case when point is on the border of the bin, assign it to the `north' then `east' child
     * NB! Node is assumed to contain the point
     */
    private Node getQuadrantForPoint(final Node node, final double lon, final double lat) {
        // assert node.containsPoint(lon, lat);
        if (node.getXc() > lon) {
            if (node.getYc() > lat) return node.getSw();
            else return node.getNw();
        } else {
            if (node.getYc() > lat) return node.getSe();
            else return node.getNe();
        }
    }

    public Node getRoot() {
        return root;
    }

    public boolean isInheritPolygons() {
        return inheritPolygons;
    }

    public void setInheritPolygons(boolean inheritPolygons) {
        this.inheritPolygons = inheritPolygons;
    }
}
