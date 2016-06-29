package org.opentripplanner.common.model;

/**
 * Created by rmkujala on 22/06/16.
 */

public class TravelTimePoint {
    public double lat;
    public double lon;
    public short timeInMinutes;

    TravelTimePoint (double lat, double lon, int timeInMinutes) {
        this.lat = lat;
        this.lon = lon;
        this.timeInMinutes = (short) timeInMinutes;
    }
}
