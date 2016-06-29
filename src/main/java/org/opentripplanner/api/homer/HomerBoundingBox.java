package org.opentripplanner.api.homer;

import com.vividsolutions.jts.geom.Coordinate;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;

import java.util.ArrayList;

/**
 * Created by rmkujala on 29/06/16.
 */
class HomerBoundingBox {
    public double lngEast;
    public double lngWest;
    public double latNorth;
    public double latSouth;

    public HomerBoundingBox(double lngEast, double lngWest, double latNorth, double latSouth) {
        this.lngEast = lngEast;
        this.lngWest = lngWest;
        this.latNorth = latNorth;
        this.latSouth = latSouth;
    }

    public ArrayList<Coordinate> getCoordinates() {
        ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
        coordinates.add(new Coordinate(lngEast, latSouth));
        coordinates.add(new Coordinate(lngWest, latSouth));
        coordinates.add(new Coordinate(lngEast, latNorth));
        coordinates.add(new Coordinate(lngWest, latNorth));
        return coordinates;
    }

    public double getMinDistance(double lat, double lng) {
        ArrayList<Coordinate> coordinates = getCoordinates();
        double minDistance = Double.MAX_VALUE;
        for (Coordinate coordinate : coordinates) {
            double distance = SphericalDistanceLibrary.distance(coordinate, new Coordinate(lng, lat));
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }
}
