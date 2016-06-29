package org.opentripplanner.api.homer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * Created by rmkujala on 29/06/16.
 */
public class HomerGeoHashTest {

    @Test
    public void getHash() {
        String hash = HomerGeoHash.getHash(-90,-180);
        assertEquals(hash.length(), 2*HomerGeoHash.hashFormatPrecision);
        assertEquals(hash.charAt(0), '0');
        assertEquals(hash.charAt(hash.length()-1), '0');

        hash = HomerGeoHash.getHash(90, 180);
        assertEquals(hash.length(), 2*HomerGeoHash.hashFormatPrecision);
        assert(hash.charAt(0) != '0');
    }

    @Test
    public void getLatIndex() {
        double lat = -90;
        assertEquals(HomerGeoHash.getLatIndex(lat), 0);
        lat = 90;
        assertEquals(HomerGeoHash.getLatIndex(lat), HomerGeoHash.gridNLat);
        assert HomerGeoHash.gridNLat > 0;
    }

    @Test
    public void getLngIndex() {
        double lng = -180;
        assertEquals(HomerGeoHash.getLngIndex(lng), 0);
        lng = 180;
        assertEquals(HomerGeoHash.getLngIndex(lng), HomerGeoHash.gridNLng);
        assert HomerGeoHash.gridNLng > 0;
    }

    @Test
    public void normalizeLongitude() {
        assertEquals(HomerGeoHash.normalizeLongitude(181), -179.0, 0.000000001);
        assertEquals(HomerGeoHash.normalizeLongitude(-181), 179.0, 0.000000001);
        assertEquals(HomerGeoHash.normalizeLongitude(180), 180.0, 0.000000001);
        assertEquals(HomerGeoHash.normalizeLongitude(-180), -180.0, 0.000000001);
        assertEquals(HomerGeoHash.normalizeLongitude(10), 10.0, 0.000000001);
    }

    @Test
    public void getNearByHashes() {
        double centerLat = 0.0;
        double centerLng = 0.0;
        ArrayList<String> hashes = HomerGeoHash.getNearByHashes(centerLat, centerLng, 2*HomerGeoHash.MIN_PRECISION_METERS);
        assert hashes.size() >= 9;
        Set hashSet = new TreeSet<String>();
        hashSet.addAll(hashes);
        // all hashes should be unique
        assertEquals(hashSet.size(), hashes.size());
    }





}