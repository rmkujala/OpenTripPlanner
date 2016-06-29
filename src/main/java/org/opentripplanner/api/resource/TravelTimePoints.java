package org.opentripplanner.api.resource;

import com.vividsolutions.jts.geom.Coordinate;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.opentripplanner.analyst.request.SampleGridRenderer;
import org.opentripplanner.analyst.request.SampleGridRequest;
import org.opentripplanner.api.common.ParameterException;
import org.opentripplanner.api.common.RoutingResource;
import org.opentripplanner.api.homer.HomerGeoHash;
import org.opentripplanner.common.geometry.ZSampleGrid;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.common.model.TravelTimePoint;
import org.opentripplanner.routing.algorithm.AStar;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static sun.security.ssl.HandshakeMessage.debug;

/**
 * Created by rmkujala on 22/06/16.
 */



@Path("/routers/{routerId}/timepoints")
public class TravelTimePoints extends RoutingResource {

    private static final Logger LOG = LoggerFactory.getLogger(org.opentripplanner.api.resource.LIsochrone.class);

    @QueryParam("maxTimeSec")
    public Integer maxTimeSec;

    @QueryParam("lat")
    public double lat;

    @QueryParam("lon")
    public double lon;

    // time in seconds after unix epoch
    @QueryParam("dateTime")
    public long dateTime;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTravelTimePoints() throws Exception {
        HashMap<String, Short> travelTimePoints = computeTravelTimePoints();
        StringWriter writer = new StringWriter();
        // Disallow caching on client side
        JSONObject.writeJSONString(travelTimePoints, writer);
        Response r = Response.ok().entity(writer.toString()).build();
        System.out.println(r.toString());
        return r;
    }

    public HashMap<String, Short> computeTravelTimePoints() throws ParameterException {
        HashMap<String, Short> homerGeoHashToTravelTime = new HashMap<>(1000000);
        // compute shortest path three SPT
        // check parameters
        RoutingRequest sptRequest = buildRequest();
        Router router = otpServer.getRouter(routerId);
        // start time
        sptRequest.dateTime = dateTime;
        sptRequest.worstTime = (sptRequest.dateTime + (sptRequest.arriveBy ? -maxTimeSec : maxTimeSec));
        sptRequest.from = new GenericLocation(lat, lon);
        sptRequest.batch = (true);
        sptRequest.setRoutingContext(router.graph);
        final ShortestPathTree spt = new AStar().getShortestPathTree(sptRequest);
        Collection<State> states = spt.getAllStates();

        for (State s : states ) {
            short mins = (short) (s.getElapsedTimeSeconds()/60);
            Coordinate coord = s.getVertex().getCoordinate();
            String hash = HomerGeoHash.getHash(coord.y, coord.x);
            if (homerGeoHashToTravelTime.containsKey(hash)) {
                if (homerGeoHashToTravelTime.get(hash) > mins) {
                    homerGeoHashToTravelTime.put(hash, mins);
                }
            } else {
                homerGeoHashToTravelTime.put(hash, mins);
            }
        }
        return homerGeoHashToTravelTime;
    }

}




