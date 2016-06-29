package org.opentripplanner.api.homer;

import com.vividsolutions.jts.geom.Coordinate;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;

import java.util.ArrayList;

/**
 * Created by rmkujala on 28/06/16.
 */
public abstract class HomerGeoHash {

    // the precision is at least 150 meters
    public static final int MIN_PRECISION_METERS = 100;
    public static final int EARTH_RADIUS_METERS = 6317_000;

    public static final int gridNLng = (int) Math.ceil(2 * Math.PI * EARTH_RADIUS_METERS/ MIN_PRECISION_METERS);
    public static final double lngStepDegrees = 360.0/gridNLng;
    public static final int gridNLat = (int) Math.ceil(Math.PI * EARTH_RADIUS_METERS/ MIN_PRECISION_METERS);
    public static final double latStepDegrees = 180.0/gridNLat;

    public static final int hashFormatPrecision = (int) Math.ceil(Math.log10(gridNLng));
    private static final String format = "%0" + String.valueOf(hashFormatPrecision) + "d";

    // map lat, lon pair to an integer hash that is easy to search for neighbors
    public static String getHash(double lat, double lng) {
        assert Math.abs(lat) <= 90;
        assert Math.abs(lng) <= 180;
        int lngIndex = getLngIndex(lng);
        int latIndex = getLatIndex(lat);

        // map grid indices to a single hash
        String lngHash = String.format(format, lngIndex);
        String latHash = String.format(format, latIndex);
        String hash = lngHash + latHash;

        return hash;
    }

    public static int getLatIndex(double lat) {
        assert Math.abs(lat) <= 90;
        double adjustedLat = (lat + 90) / 180.0;
        return (int) Math.floor(adjustedLat * gridNLat);
    }

    public static int getLngIndex(double lng) {
        assert Math.abs(lng) <= 180;
        double adjustedLng = (lng + 180) / 360.0;
        return (int) Math.floor(adjustedLng * gridNLng);
    }

    private static double getLat(int gridLatIndex) {
        return (gridLatIndex * latStepDegrees) - 90;
    }

    private static double getLng(int gridLngIndex) {
        return (gridLngIndex * lngStepDegrees) - 180 ;
    }

    private static HomerBoundingBox getBoundingBox(String hash) {
        double latCenter = getHashCenterLat(hash);
        double lngCenter = getHashCenterLng(hash);
        return getBoundingBox(latCenter, lngCenter);
    }

    public static double normalizeLongitude(double lng) {
        assert(Math.abs(lng) <= 720);
        if (lng > 180) {
            return lng - 360;
        }
        if (lng < -180) {
            return lng + 360;
        }
        return lng;
    }

    private static HomerBoundingBox getBoundingBox(double latCenter, double lngCenter) {
        double latNorth = latCenter + 0.5 * latStepDegrees;
        double latSouth = latCenter - 0.5 * latStepDegrees;
        double lngEast = (lngCenter - 0.5 * lngStepDegrees);
        lngEast = normalizeLongitude(lngEast);
        double lngWest = lngCenter - 0.5 * lngStepDegrees;
        lngWest = normalizeLongitude(lngWest);
        return new HomerBoundingBox(lngEast, lngWest, latNorth, latSouth);
    }

    private static double getHashCenterLat(String hash) {
        int gridIndexLat = Integer.parseInt(hash.substring(hashFormatPrecision));
        return getLat(gridIndexLat) + 0.5 * latStepDegrees;
    }

    private static double getHashCenterLng(String hash) {
        int gridIndexLng = Integer.parseInt(hash.substring(0, hashFormatPrecision));
        return getLng(gridIndexLng) + 0.5 * lngStepDegrees;
    }

    // for a given location, whose bounding box is at most distanceInMeters away from given (lat, lng)
    public static ArrayList<String> getNearByHashes(double lat, double lng, double distanceInMeters) {
        int latIndex = getLatIndex(lat);
        int lngIndex = getLngIndex(lng);

        double distanceLatMeters = SphericalDistanceLibrary.fastDistance(lat, lng, lat + latStepDegrees, lng, EARTH_RADIUS_METERS);
        double distanceLngMeters = SphericalDistanceLibrary.fastDistance(lat, lng, lat, lng + lngStepDegrees, EARTH_RADIUS_METERS);
        int lngSteps = (int) Math.ceil(distanceInMeters/distanceLngMeters);
        int latSteps = (int) Math.ceil(distanceInMeters/distanceLatMeters);

        ArrayList<String> hashes = new ArrayList<>(5);
        for (int i = -lngSteps; i < lngSteps+1; i++) {
            double lngNow = normalizeLongitude(lng+i*lngStepDegrees);
            for (int j = -latSteps; j < latSteps+1; j++) {
                // get bounding box
                double latNow = lat+j*latStepDegrees;
                if (Math.abs(latNow) > 90) {
                    continue;
                }
                String hash = getHash(latNow, lngNow);
                HomerBoundingBox bb = getBoundingBox(hash);
                if (bb.getMinDistance(lat, lng) < distanceInMeters) {
                    hashes.add(hash);
                }
            }
        }
        return hashes;
    }

}


