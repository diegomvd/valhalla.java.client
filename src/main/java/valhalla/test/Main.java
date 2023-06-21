import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws URISyntaxException, ValhallaException {
        ValhallaRuntimeEnvironment runtime = new ValhallaRuntimeEnvironment();

        System.out.println(runtime.getBaseURI());

        // Coordinates of sources and targets for the travel-time matrix. Note the costing parameter which essentially
        // is the transport medium.
        String input = "{\"sources\":[{\"lat\":42.544014,\"lon\":1.5163911},{\"lat\":42.524014,\"lon\":1.5263911}],\"targets\":[{\"lat\":42.539735,\"lon\":1.4988},{\"lat\":42.541735,\"lon\":1.4888}],\"costing\":\"pedestrian\"}";

        Valhalla valhalla = new Valhalla();

        ValhallaOutputDeserializer.Matrix matrix = valhalla.matrix(input);

        System.out.println(matrix.getAdjacencyList());

        List<Map<String,Number>> list = matrix.getAdjacencyList();

        Graph<String,Double> g = new DirectedSparseGraph<>();
        for (Map<String,Number> m : list){
            Integer source = (Integer) m.get("source");
            Integer target = (Integer) m.get("target");
            double time = (double) m.get("time");

            String sv = "s" + source.toString();
            String tv = "t" + target.toString();

            boolean added = g.addEdge(time,sv,tv, EdgeType.DIRECTED);
            if (!added) throw new ValhallaException("Could not add edge to graph");
        }

        System.out.println(g);


    }

}
