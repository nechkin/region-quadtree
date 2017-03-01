package org.ssn.quadtree;

import com.vividsolutions.jts.util.Stopwatch;
import org.junit.Test;

import java.util.List;

/**
 * @author Sergey Nechkin
 */
public class QuadSpatialTreeTest {

    @Test
    public void testInsert() throws Exception {
        final QuadSpatialTree rt = new QuadSpatialTree();
        rt.insert(Util.loadPoly("polygon1.txt"), 1);
        System.out.println("asd");
    }

    @Test
    public void testSearchFromNode() throws Exception {
        final QuadSpatialTree rt = new QuadSpatialTree();
        rt.insert(Util.loadPoly("polygon1.txt"), 1);
        rt.insert(Util.loadPoly("polygon3.txt"), 2);
        rt.insert(Util.loadPoly("polygon3.txt"), 3);
        rt.insert(Util.loadPoly("polygon2.txt"), 4);
        rt.insert(Util.loadPoly("polygon4.txt"), 5);

        double x = 82.823063 + (82.83405 - 82.823063) / 2;
        double y = 54.996567 + (55.045881 - 54.996567) / 2;
        // lon 0.00898 ~ 1 km.
        double km = 0.00898;

        List<Node> res;
        res = rt.searchFromNode(rt.getRoot(), 82.947967, 55.019076);
        assert(res.size() > 0);
        res = rt.searchFromNode(rt.getRoot(), x, y);
        assert(res.size() == 0);
        res = rt.searchFromNode(rt.getRoot(), x + 0.01 * km, y);
        assert(res.size() == 0);
        res = rt.searchFromNode(rt.getRoot(), x + km, y);
        assert(res.size() > 0);
        System.out.println("ok");
    }

    @Test
    public void testSearchFromNode2() throws Exception {
        final QuadSpatialTree rt = new QuadSpatialTree();
        final Stopwatch sw = new Stopwatch();
        sw.start();
        rt.insert(Util.loadShapefileNSK("RUS_adm1.shp"), "NSK");
//        for (List<Point> pp : Util.loadShapefileList("RUS_adm1.shp")) {
//            rt.insert(pp, pp.hashCode());
//            System.out.println("Inserted [" + pp.hashCode() + "]");
//        }
        sw.stop();
        System.out.println("Index built in " + sw.getTimeString());

        double x = 75.314773;
        double y = 55.111215;
        // lat 0.00898 ~ 1 km.
        double km = 0.00898;

        final Stopwatch sw2 = new Stopwatch();

        List<Node> res;
        sw2.start();
        res = rt.searchFromNode(rt.getRoot(), 82.947967, 55.019076);
        assert(res.size() > 0);
        res = rt.searchFromNode(rt.getRoot(), x, y);
        assert(res.size() > 0);
        res = rt.searchFromNode(rt.getRoot(), x + 0.01 * km, y);
        assert(res.size() > 0);
        res = rt.searchFromNode(rt.getRoot(), x + km, y);
        for (int i = 0; i < 996; ++i) {
            res = rt.searchFromNode(rt.getRoot(), x + km, y);
            assert(res.size() > 0);
        }
        sw2.stop();
        System.out.println("1000 searches in ~" + sw2.getTimeString());
    }
}
