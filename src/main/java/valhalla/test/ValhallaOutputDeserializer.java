package valhalla.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
*  Ridiculously big class to deserialize responses from the valhalla.test.Valhalla server. Only implemented a deserializer for Matrix
*  requests.
* */

public class ValhallaOutputDeserializer {

    public String json;

    public ValhallaOutputDeserializer(String json){
        this.json = json;
    }

    public ValhallaOutputDeserializer(){
        this.json = "";
    }

    public void setJson(String json){
        this.json = json;
    }


    public Matrix deserializeMatrixOutput(){
        var mapper = new ObjectMapper();
        try {
            System.out.println(this.json);
            var matrix = mapper.readValue(this.json, Matrix.class);
            System.out.println(matrix);
            return matrix;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }


    static public class Matrix{

        public String algorithm;
        public String units;
        public ArrayList<ArrayList<Coordinates>> sources;
        public ArrayList<ArrayList<Coordinates>> targets;
        public Collection<Collection<PairwiseDistance>> sourcesToTargets;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Matrix(
                @JsonProperty("algorithm") String algorithm,
                @JsonProperty("units") String units,
                @JsonProperty("sources") ArrayList<ArrayList<Coordinates>> sources,
                @JsonProperty("targets") ArrayList<ArrayList<Coordinates>> targets,
                @JsonProperty("sources_to_targets") Collection<Collection<PairwiseDistance>> sourcesToTargets){
            this.algorithm = algorithm;
            this.units = units;
            this.sources = sources;
            this.targets = targets;
            this.sourcesToTargets = sourcesToTargets;
        }

        @JsonProperty("algorithm")
        public String algorithm() {
            return algorithm;
        }
        @JsonProperty("units")
        public String units() {
            return units;
        }
        @JsonProperty("sources")
        public ArrayList<ArrayList<Coordinates>> sources() {
            return sources;
        }
        @JsonProperty("targets")
        public ArrayList<ArrayList<Coordinates>> targets() {
            return targets;
        }
        @JsonProperty("sources_to_targets")
        public Collection<Collection<PairwiseDistance>> sourcesToTargets() {
            return sourcesToTargets;
        }


        public String getAlgorithm(){
            return this.algorithm;
        }
        public String getUnits(){
            return this.algorithm;
        }
        public List<Map<String,Double>> getSources(){
            return this.sources.get(0).stream().map(Coordinates::exportAsMap).collect(Collectors.toList());
        }
        public List<Map<String,Double>> getTargets(){
            return this.sources.get(0).stream().map(Coordinates::exportAsMap).collect(Collectors.toList());
        }

        public List<Map<String,Number>> getAdjacencyList(){
            return this.sourcesToTargets
                    .stream()
                    .flatMap(x -> x.stream().map(PairwiseDistance::exportAsMap))
                    .collect(Collectors.toList());
        }

    }

    public static class Coordinates{
        public double lon;
        public double lat;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Coordinates(
                @JsonProperty("lon") double lon,
                @JsonProperty("lat") double lat){
            this.lon = lon;
            this.lat = lat;
        }
        @JsonProperty("lon")
        public double lon() {
            return lon;
        }
        @JsonProperty("lat")
        public double lat() {
            return lat;
        }

        public Map<String,Double> exportAsMap(){
            return Map.of("lon", lon, "lat", lat);
        }
    }

    public static class PairwiseDistance{
        public double distance;
        public double time;
        public int targetId;
        public int sourceId;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public PairwiseDistance(
                @JsonProperty("distance") double distance,
                @JsonProperty("time") double time,
                @JsonProperty("to_index") int targetId,
                @JsonProperty("from_index") int sourceId){
            this.distance = distance;
            this.time = time;
            this.targetId = targetId;
            this.sourceId = sourceId;
        }
        @JsonProperty("distance")
        public double distance() {
            return distance;
        }
        @JsonProperty("time")
        public double time() {
            return time;
        }
        @JsonProperty("to_index")
        public int targetId() {
            return targetId;
        }

        @JsonProperty("from_index")
        public int sourceId() {
            return sourceId;
        }

        public Map<String,Number> exportAsMap(){
            return Map.of("source", sourceId, "target", targetId, "distance", distance, "time", time);
        }
    }
}
