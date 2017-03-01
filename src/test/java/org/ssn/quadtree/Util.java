package org.ssn.quadtree;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey Nechkin
 */
public class Util {
    public static List<Point> loadPoly(final String source) throws IOException {
        final List<Point> poly = new ArrayList<Point>();
        final Class<?> clazz = Util.class;

        try(InputStream in = clazz.getResourceAsStream(source);
            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            for(String line; (line = br.readLine()) != null; ) {
                final String point[]  = line.split(",");
                poly.add(new Point(Double.parseDouble(point[0]), Double.parseDouble(point[1])));
            }
        }

        return poly;
    }

    public static List<Point> loadShapefileNSK(final String source) throws IOException {
        // http://docs.geotools.org/latest/userguide/library/referencing/order.html
        // http://docs.geotools.org/stable/userguide/tutorial/geometry/geometrycrs.html
        System.setProperty("org.geotools.referencing.forceXY", "true");

        final Class<?> clazz = Util.class;
        URL url = clazz.getResource(source);

        Map connect = new HashMap();
        connect.put("url", url);

        DataStore dataStore = DataStoreFinder.getDataStore(connect);
        String[] typeNames = dataStore.getTypeNames();
        String typeName = typeNames[0];

        System.out.println("Reading type " + typeName);

        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
        SimpleFeatureCollection collection = featureSource.getFeatures();
        SimpleFeatureIterator iterator = collection.features();

        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                if (!feature.getID().equals("RUS_adm1.55")) continue;
                return poly2points(feature);
            }
        } finally {
            iterator.close();
        }

        return null;
    }

    public static List<List<Point>> loadShapefileList(final String source) throws IOException {
        // http://docs.geotools.org/latest/userguide/library/referencing/order.html
        // http://docs.geotools.org/stable/userguide/tutorial/geometry/geometrycrs.html
        System.setProperty("org.geotools.referencing.forceXY", "true");

        final Class<?> clazz = Util.class;
        URL url = clazz.getResource(source);

        Map connect = new HashMap();
        connect.put("url", url);

        DataStore dataStore = DataStoreFinder.getDataStore(connect);
        String[] typeNames = dataStore.getTypeNames();
        String typeName = typeNames[0];

        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
        SimpleFeatureCollection collection = featureSource.getFeatures();
        SimpleFeatureIterator iterator = collection.features();

        List<List<Point>> multi = new ArrayList<>();
        iterator.next(); // XXX skip first with all geoms

        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                multi.add(poly2points(feature));
            }
        } finally {
            iterator.close();
        }

        return multi;
    }

    private static List<Point> poly2points(SimpleFeature feature) {
        List<Point> points = new ArrayList<>();
        MultiPolygon mp = (MultiPolygon)feature.getAttribute(0);
        int n = mp.getNumGeometries();
        for ( int i = 0 ; i < n ; i++ ) {
            Geometry g = mp.getGeometryN(i);
            Coordinate[] coords = g.getCoordinates();
            for (Coordinate c : coords)
            points.add(new Point(c.x, c.y));
        }

        return points;
    }
}