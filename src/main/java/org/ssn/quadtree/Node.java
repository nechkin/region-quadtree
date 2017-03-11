package org.ssn.quadtree;

import java.util.*;

/**
 * @author Sergey Nechkin
 */
public class Node {

    public static final double EPS = 1e-9;

    private double xc;    // x center
    private double yc;    // y center
    private double halfW; // half width
    private double halfH; // half height
    private Node parent;
    // private List<Polygon> polygons;  // Regions tree doesn't require reinsert of the polygons, hence no need to save the shape
    private List<Object> polygonValues; // could have taken it from polygons, but we want to reduce lookups
                                        // supposed to be a list of region ids
    private Node nw;
    private Node ne;
    private Node sw;
    private Node se;

    private int level;

    public Node(double xc, double yc, double halfW, double halfH, Node parent) {
        this.xc = xc;
        this.yc = yc;
        this.halfW = halfW;
        this.halfH = halfH;

        this.parent = parent;
        if (parent != null) {
            level = parent.getLevel() + 1;
        } else {
            level = 0;
        }
    }

    /**
     * Check relation of the polygon A to this Node
     * -2 A intersects Node
     * -1 A inside Node
     * 0 A outside of Node
     * 1 A contains Node
     * NB! last point in points should be the same as the first point
     */
    public int collisionType(final List<Point> points) {
        // Check if any of A segment intersect this or whether all A's points are inside the node
        boolean aIntersects = false;
        boolean aIsInside = true;

        final double minX = xc - halfW;
        final double maxX = xc + halfW;
        final double minY = yc - halfH;
        final double maxY = yc + halfH;
        final Point pnw = new Point(minX, maxY);
        final Point pne = new Point(maxX, maxY);
        final Point psw = new Point(minX, minY);
        final Point pse = new Point(maxX, minY);
        for (int i = 0; i < points.size() - 1; i++) {
            final Point p1 = points.get(i);
            final Point p2 = points.get(i + 1);
            // Check whether start point for each segment is inside the Node
            if (!containsPoint(p1.x, p1.y)) aIsInside = false;

            // Check if segment intersect the Node
            final double pMinX = p1.x < p2.x ? p1.x : p2.x;
            final double pMaxX = p1.x > p2.x ? p1.x : p2.x;
            final double pMinY = p1.y < p2.y ? p1.y : p2.y;
            final double pMaxY = p1.y > p2.y ? p1.y : p2.y;

            // NB! should happen before the intersection check
            // NB! inequalities are strict
            if (!(minX < pMaxX && maxX > pMinX &&
                    minY < pMaxY && maxY > pMinY)) {
                continue;
            }

            if (intersect(psw, pnw, p1, p2) || intersect(pnw, pne, p1, p2)
                    || intersect(pne, pse, p1, p2) || intersect(pse, psw, p1, p2)) {
                aIntersects = true;
            }
        }

        if (aIsInside) return -1;
        if (aIntersects) return -2;

        // If got here then if A contains at least one point of the Node, then it contains the Node
        // due to A neither intersects nor is inside the Node
        if (polygonContainsPointForSquares(points, xc - halfW + EPS, yc - halfH + EPS)) return 1;
        else return 0;

    }

    public static double vectProduct(Point A, Point B, Point C) {
        return (B.x-A.x) *(C.y-A.y) - (B.y-A.y) * (C.x-A.x);
    }

    /**
     * Check if lines AB and CD intersect.
     * Collinear lines considered non-intersecting
     */
    public static boolean intersect(Point A, Point B, Point C, Point D) {
        double vp1 = vectProduct(A,B,C);
        double vp2 = vectProduct(A,B,D);
        if (Math.abs(vp1) < EPS && Math.abs(vp2) < EPS) return false;

        double vp3 = vectProduct(A,C,D);
        double vp4 = vectProduct(B,C,D);

        return (vp1 > 0 ? vp2 <= 0 : vp2 > 0) &&
                (vp3 > 0 ? vp4 <= 0 : vp4 > 0);
    }

    public boolean isLeaf() {
        return nw == null;
    }

    public boolean containsPoint(final double x, final double y) {
        return (xc - halfW) <= x && (xc + halfW) >= x
                && (yc - halfH) <= y && (yc + halfH) >= y;
    }

    // not tested
    public static boolean polygonContainsPointForSquares(final List<Point> polygon,
                                                         final double pointX, final double pointY) {
        boolean result = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            final Point pi = polygon.get(i);
            final Point pj = polygon.get(j);
            if ((pi.y > pointY) != (pj.y > pointY) &&
                    (pointX < (pj.x - pi.x) * (pointY - pi.y) / (pj.y - pi.y) + pi.x)) {
//                    (pointX - pi.x) * (pj.y - pi.y) < (pj.x - pi.x) * (pointY - pi.y)) {
                result = !result;
            }
        }
        return result;
    }

    /**
     * Ray casting algorithm.
     * For each polygon segments:
     * Check if the horizontal ray from point P(pointX, pointY) directed to the right intersects a segment
     * 1. (pi.y > pointYeps) == (pj.y > pointYeps) - check that polygon segment is horizontal or that point P
     * ordinate is not between segment's vertexes ordinates
     * 2. pointY == pj.y ? pointY + EPS : pointY - to address the problem when the ray passes exactly through a
     * vertex of a polygon. If the intersection point is a vertex of a tested polygon side, then the intersection
     * counts only if the second (pj) vertex of the side lies above the ray. This is effectively equivalent to
     * considering vertices on the ray as lying slightly above the ray, if these vertices are the second point
     * of the tested segment.
     * 3. pointX > Double.max(pi.x, pj.x) - check that point is to the right of the segment
     * 4. (pj.x - pi.x) * (pointYeps - pi.y) >= (pointX - pi.x) * (pj.y - pi.y) - using cross product
     * (на самом деле псевдоскалярное произведения, т.к. мы в 2D) to determine whether point P is to the left
     * of the segment and thus the ray intersect the segment
     */
    public static boolean polygonContainsPoint(final List<Point> polygon,
                                               final double pointX, final double pointY) {
        //
        boolean result = false;
        for (int i = 0; i < polygon.size() - 1; ++i) {
            final Point pi;
            final Point pj;
            int j = i + 1;
            // point pi should be below point pj for cross product check to works properly
            if (polygon.get(i).y < polygon.get(j).y) {
                pi = polygon.get(i);
                pj = polygon.get(j);
            } else {
                pi = polygon.get(j);
                pj = polygon.get(i);
            }

            double pointYeps = pointY == pj.y ? pointY + EPS : pointY;

            if ((pi.y > pointYeps) == (pj.y > pointYeps) || pointX > Double.max(pi.x, pj.x)) {
                continue;
            }

            if ((pj.x - pi.x) * (pointYeps - pi.y) >= (pointX - pi.x) * (pj.y - pi.y)) {
                result = !result;
            }
        }
        return result;
    }

    public double getXc() {
        return xc;
    }

    public void setXc(double xc) {
        this.xc = xc;
    }

    public double getYc() {
        return yc;
    }

    public void setYc(double yc) {
        this.yc = yc;
    }

    public double getHalfW() {
        return halfW;
    }

    public void setHalfW(double halfW) {
        this.halfW = halfW;
    }

    public double getHalfH() {
        return halfH;
    }

    public void setHalfH(double halfH) {
        this.halfH = halfH;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Object> getPolygonValues() {
        return polygonValues;
    }

    public void setPolygonValues(List<Object> polygonValues) {
        this.polygonValues = polygonValues;
    }

    public Node getNw() {
        return nw;
    }

    public void setNw(Node nw) {
        this.nw = nw;
    }

    public Node getNe() {
        return ne;
    }

    public void setNe(Node ne) {
        this.ne = ne;
    }

    public Node getSw() {
        return sw;
    }

    public void setSw(Node sw) {
        this.sw = sw;
    }

    public Node getSe() {
        return se;
    }

    public void setSe(Node se) {
        this.se = se;
    }

    public void inheritPolygons() {
        final Set<Object> uniqueValues = new HashSet<>();

        Node parent = getParent();
        while (parent != null) {
            List<Object> parentPolygons = parent.getPolygonValues();
            if (parentPolygons != null) {
                uniqueValues.addAll(parentPolygons);
            }
            parent = parent.getParent();
        }
        if (uniqueValues.size() > 0) {
            if (getPolygonValues() == null) {
                // polygon values list is null when there are no values assigned to the polygon
                setPolygonValues(new ArrayList<>());
            }
            getPolygonValues().addAll(uniqueValues);
        }

        if (!isLeaf()) {
            getNw().inheritPolygons();
            getNe().inheritPolygons();
            getSe().inheritPolygons();
            getSw().inheritPolygons();
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
