import org.ssn.quadtree.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sergey Nechkin
 */
public class RegionsTreePainter {

    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        final TreePane treePane = new TreePane();
        final JScrollPane jScrollPane = new JScrollPane(treePane);

        //noinspection Convert2Lambda
        jScrollPane.getViewport().addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
//              double delta = -0.5f * e.getPreciseWheelRotation();
//              double prevScale = treePane.scale;
//              treePane.scale += delta * treePane.scale;

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
                int newX = mouseX - (int) ((mouseX - treePane.getX() + 0.0) * scaleFactor + 0.5);
                int newY = mouseY - (int) ((mouseY - treePane.getY() + 0.0) * scaleFactor + 0.5);
                treePane.setLocation(newX, newY);

                viewport.validate();
            }
        });

        jScrollPane.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                java.awt.Point point = e.getPoint();
                double lon = treePane.xToLongitude(point.getX());
                double lat = treePane.yToLatitude(point.getY());
                List<Node> nodes = treePane.rt.searchFromNode(treePane.root, lon, lat);
                String smallestNodeValues = "";
                if (nodes.size() > 0) {
                    smallestNodeValues = nodes.get(nodes.size() - 1).getPolygonValues().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));
                }
                JOptionPane.showMessageDialog(jScrollPane, String.format("Lon: %.6f, Lat: %.6f, Nodes: %s", lon, lat,
                        smallestNodeValues));
            }
        });

        EventQueue.invokeLater(() -> {
            final JFrame frame = new JFrame("Regions tree");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            frame.add(jScrollPane);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }


}

