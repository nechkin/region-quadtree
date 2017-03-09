import org.ssn.quadtree.Node;
import org.ssn.quadtree.Point;
import org.ssn.quadtree.QuadSpatialTree;
import org.ssn.quadtree.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Nechkin
 */

public class RegionsTreePainter {

    public static void main(String[] args) {
        new RegionsTreePainter();
    }

    public RegionsTreePainter() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                final JFrame frame = new JFrame("Regions tree");
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                try {
                    final TreePane treePane = new TreePane();
                    final JScrollPane jScrollPane = new JScrollPane(treePane);

                    jScrollPane.getViewport().addMouseWheelListener(new MouseWheelListener() {
                        @Override
                        public void mouseWheelMoved(MouseWheelEvent e) {
//                            double delta = -0.5f * e.getPreciseWheelRotation();
//                            double prevScale = treePane.scale;
//                            treePane.scale += delta * treePane.scale;

                            double scaleFactor = e.getWheelRotation() > 0 ? 0.5 : 2.0;
                            treePane.scale = scaleFactor * treePane.scale;

                            JViewport viewport = jScrollPane.getViewport();
                            java.awt.Point mp = e.getPoint();

                            // Mouse coordinates in the Viewport system
                            int mouseX = mp.x - viewport.getX();
                            int mouseY = mp.y - viewport.getY();

                            // How much to scale relative to the previous Viewport's view (TreePane) size
                            //double scaleFactor = treePane.scale / prevScale;

                            // New coordinates of the Viewport's view (that is TreePane)
                            int newX = mouseX - (int)((mouseX - treePane.getX() + 0.0) * scaleFactor + 0.5);
                            int newY = mouseY - (int)((mouseY - treePane.getY() + 0.0) * scaleFactor + 0.5);
                            treePane.setLocation(newX, newY);

                            viewport.validate();
                        }
                    });

                    jScrollPane.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            System.out.println(treePane.getPreferredSize());
                            System.out.println(jScrollPane.getLocation());
                            java.awt.Point point = e.getPoint();
                            double x2 = (point.getX() - treePane.getX()) / treePane.scale;
                            double y2 = (point.getY() - treePane.getY()) / treePane.scale;
                            int lon = (int)(x2 / treePane.pxPerUnitX - treePane.nodeOffsetX + 0.5);
                            int lat = - (int)(y2 / treePane.pxPerUnitY - treePane.nodeOffsetY + 0.5);
                            List<Node> nodes = treePane.rt.searchFromNode(treePane.root, lon, lat);

                        }
                    });

                    frame.add(jScrollPane);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class TreePane extends JPanel /*implements Scrollable*/ {

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
            rt.insert(new ArrayList<Point>(poly0), 0);
            rt.insert(new ArrayList<Point>(poly1), 1);
            rt.insert(new ArrayList<Point>(poly2), 2);
            rt.insert(new ArrayList<Point>(poly3), 3);

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
            //g.drawLine(10,10,150,150); // Draw a line from (10,10) to (150,150)
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
            g2d.drawOval(calcX(treeX(82.947967)), calcY(treeY(55.019076)), 5, 5); // NSK

//            double x = 82.823063 + (82.83405 - 82.823063) / 2;
//            double y = 54.996567 + (55.045881 - 54.996567) / 2;
//            double km = 0.00898;
//            g2d.drawOval(calcX(treeX(x + 0.61 * km)), calcY(treeY(y)), 5, 5); // NSK

            g2d.dispose();
        }

        private void drawRegion(List<Point> points) {
            g2d.setColor(Color.RED);
            for (int i = 0; i < points.size() - 1; i++) {
                final Point p1 = points.get(i);
                final Point p2 = points.get(i + 1);
                g2d.drawLine(calcX(treeX(p1.x)), calcY(treeY(p1.y)),
                             calcX(treeX(p2.x)), calcY(treeY(p2.y)));
            }
        }

        private void drawNode(Node node, List<int[]> quadrantsWithinPolygons) {
            if (node.isLeaf()) {
                int x = calcX(treeX(node.getXc() - node.getHalfW()));
                int y = calcY(treeY(node.getYc() + node.getHalfH()));
                int w = (int) (node.getHalfW() * 2.0 * pxPerUnitX * scale + 0.5);
                int h = (int) (node.getHalfH() * 2.0 * pxPerUnitY * scale + 0.5);

                if (node.getPolygonValues() != null && node.getPolygonValues().size() > 0) {
//                int x1 = calcX(treeX(node.getXc()));
//                int y1 = calcY(treeY(node.getYc()));
//                g2d.drawString("" + node.getXc(), x1, y1);
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

        private double treeX(double x) {
            return (x + nodeOffsetX) * scale;
        }

        private double treeY(double y) {
            return (-y + nodeOffsetY) * scale;
        }

        private int calcX(double x) {
            return (int)(pxPerUnitX * x + 0.5) + offset;
        }

        private int calcY(double y) {
            return (int)(pxPerUnitY * y + 0.5) + offset;
        }
    }
}

