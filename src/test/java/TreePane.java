import org.ssn.quadtree.Node;
import org.ssn.quadtree.Point;
import org.ssn.quadtree.QuadSpatialTree;
import org.ssn.quadtree.Util;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Nechkin
 */
public class TreePane extends JPanel {

    private static final int pxW = 800;
    private static final int pxH = 600;
    public static final int offset = 0;

    public QuadSpatialTree rt;
    public Node root;
    public double pxPerUnitX;
    public double pxPerUnitY;

    public double nodeOffsetX;
    public double nodeOffsetY;

    public List<List<Point>> poligons;

    private Graphics2D g2d;

    public double scale = 1.0;//e-5f;

    public TreePane() throws IOException {
        super();
        final QuadSpatialTree rt = new QuadSpatialTree();
        this.rt = rt;
        final List<Point> poly0 = Util.loadPoly("polygon0.txt");
        final List<Point> poly1 = Util.loadPoly("polygon1.txt");
        final List<Point> poly2 = Util.loadPoly("polygon2.txt");
        final List<Point> poly3 = Util.loadPoly("polygon3.txt");
        rt.insert(new ArrayList<>(poly0), 0);
        rt.insert(new ArrayList<>(poly1), 1);
        rt.insert(new ArrayList<>(poly2), 2);
        rt.insert(new ArrayList<>(poly3), 3);

        poligons = new ArrayList<>();

        // 89 regions
//            for (List<org.ssn.quadtree.Point> pp : Util.loadShapefileList("RUS_adm1.shp")) {
//                rt.insert(pp, pp.hashCode());
//                System.out.println("Inserted [" + pp.hashCode() + "]");
//                pp.add(pp.get(0));
//                poligons.add(pp);
//            }

        // NSK
//            final List<org.ssn.quadtree.Point> polyNsk = Util.loadShapefileNSK("RUS_adm1.shp");
//            rt.insert(polyNsk, "NSK");

        poly0.add(poly0.get(0));
        poly1.add(poly1.get(0));
        poly2.add(poly2.get(0));
        poly3.add(poly3.get(0));
        poligons.add(poly0);
        poligons.add(poly1);
        poligons.add(poly2);
        poligons.add(poly3);

//            polyNsk.add(polyNsk.get(0));
//            poligons.add(polyNsk);

        root = rt.getRoot();
        pxPerUnitX = pxW / (root.getHalfW() * 2.0);
        pxPerUnitY = pxH / (root.getHalfH() * 2.0);
        nodeOffsetX = root.getHalfW();
        nodeOffsetY = root.getHalfH();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = new Dimension();
        size.width = (int)Math.round((pxW + 2 * offset) * scale);
        size.height = (int)Math.round((pxH + 2 * offset) * scale);
        return size;
    }

    /*public void paint(Graphics g){
        this.g = g;
        //g.drawLine(10,10,150,150);
        drawNode(root);
        drawRegion(poly);
    }*/

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        //g.getClipBounds();

        Graphics2D g2d = (Graphics2D) g.create();
        //AffineTransform at = new AffineTransform();
        //at.scale(scale, scale);
        //g2d.setTransform(at);

        this.g2d = g2d;
        final ArrayList<int[]> greenPolygons = new ArrayList<>();
        drawNode(root, greenPolygons);
        g2d.setColor(Color.GREEN);
        greenPolygons.forEach(r -> g2d.drawRect(r[0], r[1], r[2], r[3]));
        g2d.setColor(Color.BLACK);

        for (List<Point> p : poligons) {
            drawRegion(p);
        }
//        g2d.drawOval(toX(82.947967), toY(55.019076), 5, 5); // NSK

            double x = 82.823063 + (82.83405 - 82.823063) / 2;
            double y = 54.996567 + (55.045881 - 54.996567) / 2;
            double km = 0.00898;
//            g2d.drawOval(toX(x + 0.61 * km), toY(y), pxPerUnitX * km, pxPerUnitX * km); // NSK
            g2d.drawOval(toX(82.947967), toY(55.019076), distToX(km), distToY(km)); // NSK
            g2d.drawOval(toX(82.947967), toY(0.0), distToX(km), distToY(km)); // NSK

        g2d.dispose();
    }

    private void drawRegion(List<Point> points) {
        g2d.setColor(Color.RED);
        for (int i = 0; i < points.size() - 1; i++) {
            final Point p1 = points.get(i);
            final Point p2 = points.get(i + 1);
            g2d.drawLine(toX(p1.x), toY(p1.y),
                    toX(p2.x), toY(p2.y));
        }
    }

    private void drawNode(Node node, List<int[]> quadrantsWithinPolygons) {
        if (node.isLeaf()) {
            int x = toX(node.getXc() - node.getHalfW());
            int y = toY(node.getYc() + node.getHalfH());
            int w = (int) (node.getHalfW() * 2.0 * pxPerUnitX * scale + 0.5);
            int h = (int) (node.getHalfH() * 2.0 * pxPerUnitY * scale + 0.5);

            if (node.getPolygonValues() != null && node.getPolygonValues().size() > 0) {
//                    int x1 = toX(node.getXc()));
//                    int y1 = toY(node.getYc()));`
//                    g2d.drawString("" + node.getXc(), x1, y1);
                // Green polygons should be on top of black
//                    g2d.setColor(Color.GREEN);
                quadrantsWithinPolygons.add(new int[]{x, y, w, h});

            } else {
                g2d.drawRect(x, y, w, h);
            }
//                g2d.setColor(Color.BLACK);
        }

        if (!node.isLeaf()) {
            drawNode(node.getNw(), quadrantsWithinPolygons);
            drawNode(node.getSw(), quadrantsWithinPolygons);
            drawNode(node.getNe(), quadrantsWithinPolygons);
            drawNode(node.getSe(), quadrantsWithinPolygons);
        }
    }

    private int toX(double lon) {
        return calcX(treeX(lon));
    }

    private int toY(double lat) {
        return calcY(treeY(lat));
    }

    /**
     * Convert longitude (in the coordinate system with center at 0/0 lon/lat) into coordinate system with the
     * center in the upper-left corner.
     * Takes scale into account.
     * @param x longitude
     */
    private double treeX(double x) {
        return (x + nodeOffsetX) * scale;
    }

    /**
     * Convert latitude (in the coordinate system with center at 0/0 lon/lat) into coordinate system with the
     * center in the upper-left corner.
     * Takes scale into account.
     * @param y latitude
     */
    private double treeY(double y) {
        return (-y + nodeOffsetY) * scale;
    }

    /**
     * Convert x coordinate to pixels
     */
    private int calcX(double x) {
        return (int)(pxPerUnitX * x + 0.5) + offset;
    }

    /**
     * Convert y coordinate to pixels
     */
    private int calcY(double y) {
        return (int)(pxPerUnitY * y + 0.5) + offset;
    }

    private int distToX(double dist) {
        return (int)(pxPerUnitX * dist * scale + 0.5) + offset;
    }

    private int distToY(double dist) {
        return (int)(pxPerUnitY * dist * scale + 0.5) + offset;
    }

    /**
     * @param x coordinate in displayed panel (parent container of this panel) coordinate system
     * @return longitude, that corresponds to the x coordinate
     */
    public double xToLongitude(double x) {
        // Following is a reverse of calcX(treeX(x));
        double x2 = (x - getX()) / scale;
        return x2 / pxPerUnitX - nodeOffsetX;
    }

    /**
     * @param y coordinate in displayed panel (parent container of this panel) coordinate system
     * @return latitude, that corresponds to the y coordinate
     */
    public double yToLatitude(double y) {
        // Following is a reverse of calcY(treeY(x));
        double y2 = (y - getY()) / scale;
        return - (y2 / pxPerUnitY - nodeOffsetY);
    }
}
