package org.opentripplanner.api.resource;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.api.common.ParameterException;
import org.opentripplanner.api.parameter.QualifiedModeSet;
import org.opentripplanner.graph_builder.GraphBuilder;
import org.opentripplanner.routing.error.GraphNotFoundException;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.DefaultStreetVertexIndexFactory;
import org.opentripplanner.routing.impl.InputStreamGraphSource;
import org.opentripplanner.routing.impl.MemoryGraphSource;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.standalone.CommandLineParameters;
import org.opentripplanner.standalone.OTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import static org.junit.Assert.*;


/**
 * Created by rmkujala on 22/06/16.
 */
public class TravelTimePointsTest {

    private TravelTimePoints travelTimePointsApi;
    private static final Logger LOG = LoggerFactory.getLogger(TravelTimePointsTest.class);

    @Before
    public void setUp() {
        CommandLineParameters params = new CommandLineParameters();
        params.inMemory = true;

        OTPServer otpServer = new OTPServer(new CommandLineParameters(), new GraphService());

        GraphService graphService = otpServer.getGraphService();
        try {
            LOG.info("looking for a graph:");
            InputStreamGraphSource graphSource = InputStreamGraphSource.newFileGraphSource(
                    "tampere", new File("src/test/resources/tampere/"), Graph.LoadLevel.FULL);
            graphService.registerGraph("tampere", graphSource);
            // for testing that the graph exists
            Graph g = graphService.getRouter("tampere").graph;
            assert g != null;
            LOG.info("graph found!");
        } catch (GraphNotFoundException e) {
            System.out.println("The construction of the graph should last for something like max. 10 minutes, if it" +
                    "takes longer, there probably is not enough memory available.");
            GraphBuilder graphBuilder = GraphBuilder.forDirectory(params, new File("src/test/resources/tampere/"));
            graphBuilder.serializeGraph = true;
            graphBuilder.run();
            Graph graph = graphBuilder.getGraph();
            graph.index(new DefaultStreetVertexIndexFactory());
            graphService.registerGraph("tampere", new MemoryGraphSource("tampere", graph));
            LOG.info("graph constructed!");
        }

        travelTimePointsApi = new TravelTimePoints();
        travelTimePointsApi.otpServer = otpServer;
        travelTimePointsApi.routerId = "tampere";

        // center point for tampere:
        travelTimePointsApi.lat = 61.494148;
        travelTimePointsApi.lon = 23.755438;
        travelTimePointsApi.maxTimeSec = 7200;
        travelTimePointsApi.dateTime = 1376296338;
    }

    @Test
    public void testTravelTimePoints() throws ParameterException {
        HashMap<String, Short> travelTimePoints = travelTimePointsApi.computeTravelTimePoints();
        assert travelTimePoints.size() > 0;
        for (String hash : travelTimePoints.keySet()) {
            assert travelTimePoints.get(hash) > 0;
            // System.out.printf(hash + " ");
            // System.out.println(travelTimePoints.get(hash));
        }
    }

    @Test
    public void testTravelTimePointsRestApi() throws Exception {
        travelTimePointsApi.modes = new QualifiedModeSet("WALK,TRANSIT");
        javax.ws.rs.core.Response response = travelTimePointsApi.getTravelTimePoints();
        assertNotNull(response);
        int stringLengthTransit = ((String) (response.getEntity())).length();
        assert stringLengthTransit > 0;

        travelTimePointsApi.modes = new QualifiedModeSet("CAR");
        response = travelTimePointsApi.getTravelTimePoints();
        assertNotNull(response);
        int stringLengthCar = ((String) (response.getEntity())).length();
        assert stringLengthCar > stringLengthTransit;
    }



}

