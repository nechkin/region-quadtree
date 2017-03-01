package org.ssn.quadtree;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NodeTest {

    final static List<Point> square = fromArray(new double[][] {{0, 0}, {20, 0}, {20, 20}, {0, 20}, {0, 0}});
    final static List<Point> squareHole = fromArray(new double[][] {{0, 0}, {20, 0}, {20, 20}, {0, 20},
            {5, 5}, {15, 5}, {15, 15}, {5, 15}, {0, 0}});
    final static List<Point> strange = fromArray(new double[][] {{0, 0}, {5, 5}, {0, 20}, {5, 15}, {15, 15},
            {20, 20}, {20, 0}, {0, 0}});
    final static List<Point> hexagon = fromArray(new double[][] {{6, 0}, {14, 0}, {20, 10}, {14, 20},
            {6, 20}, {0, 10}, {6, 0}});

    final static List[] shapes = {square, squareHole, strange, hexagon};

    private static List<Point> fromArray(double[][] points) {
        final List<Point> result = new ArrayList<>();
        for (double[] point : points) {
            result.add(new Point(point[0], point[1]));
        }
        return result;
    }

    @Test
    public void testPolygonContainsPoint() throws Exception {
        double[][] testPoints = {{10, 10}, {10, 16}, {-20, 10}, {0, 10},
                {20, 10}, {16, 10}, {20, 20}};

        for (List shape : shapes) {
            for (double[] point : testPoints)
                //noinspection unchecked
                System.out.printf("%7s ", Node.polygonContainsPoint((List<Point>) shape, point[0], point[1]));
            System.out.println();
        }
    }
}