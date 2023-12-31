package valhalla.test;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.List;
import java.util.Map;


/**
 * Simple example of creation of an accessibility graph using valhalla.test.Valhalla's Matrix API.
 */
public class Main {

    public static void main(String[] args) throws ValhallaException {

        // valhalla.test.Valhalla Java peer. Connected to "http://localhost:8002" by default.
        Valhalla valhalla = new Valhalla();

        /*
        * Matrix API example.
        * */

        // Coordinates of sources and targets for the travel-time matrix. Note the costing parameter which essentially
        // is the means of transport. For testing make sure that coordinates are within the loaded OSM environment.
        String input = "{\"sources\":[{\"lat\":42.544014,\"lon\":1.5163911},{\"lat\":42.524014,\"lon\":1.5263911}],\"targets\":[{\"lat\":42.539735,\"lon\":1.4988},{\"lat\":42.541735,\"lon\":1.4888}],\"costing\":\"pedestrian\"}";

        // Call to matrix method with input, the function returns the deserialized JSON string in a specific format.
        ValhallaOutputDeserializer.Matrix matrix = valhalla.matrix(input);

        // The adjacency list stores information on the distance/time between each source and target in a way that is
        // very friendly for graph creation with JUNG, and probably also with JGraphT.
        List<Map<String,Number>> list = matrix.getAdjacencyList();
        System.out.println(list);

        // Instantiate and populate the graph.
        Graph<String,Double> g = new DirectedSparseGraph<>();
        for (Map<String,Number> m : list){
            Integer source = (Integer) m.get("source");
            Integer target = (Integer) m.get("target");
            double time = (double) m.get("time");

            // VertexIds are transformed to strings and prefixed with s or t to easily differentiate between sources and
            // targets as the index starts at 0 for both. If needed to use integers ewe can always do
            // target_index += max(source_index)
            String sv = "s" + source.toString();
            String tv = "t" + target.toString();

            // In this case a time accessibility graph is created.
            boolean added = g.addEdge(time,sv,tv, EdgeType.DIRECTED);

            if (!added) throw new ValhallaException("Could not add edge to graph");
        }
        System.out.println(g);

        /*
         * Optimized Route API example.
         * */

        // This is a back and forth trip in Andorra.
        input = "{\"locations\":[{\"lat\":42.544014,\"lon\":1.5163911},{\"lat\":42.539735,\"lon\":1.4988},{\"lat\":42.544014,\"lon\":1.5163911}],\"costing\":\"auto\"}";

        // Call to optimized route method with input, the function returns the deserialized JSON string in a specific format.
        ValhallaOutputDeserializer.OptimizedRoute route = valhalla.optimized_route(input);
        List<String> path = route.getPath();
        Map<String,Double> stats = route.getSummaryStatistics();
        List<Map<String,Number>> waypoints = route.getWaypoints();

        System.out.println(path);
        System.out.println(stats);
        System.out.println(waypoints);
    }

}
