package com.nta.dto.response;
import java.util.List;

public class DurationBingMapApiResponse {
    public String authenticationResultCode;
    public List<ResourceSet> resourceSets;

    public static class ResourceSet {
        public int estimatedTotal;
        public List<Resource> resources;
    }

    public static class Resource {
//        public List<Double> bbox;
        public String distanceUnit;
        public String durationUnit;
        public double travelDistance;
        public int travelDuration;
//        public List<RouteLeg> routeLegs;
    }

//    public static class RouteLeg {
//        public Point actualEnd;
//        public Point actualStart;
//        public String description;
//        public List<ItineraryItem> itineraryItems;
//    }
//
//    public static class Point {
//        public String type;
//        public List<Double> coordinates;
//    }
//
//    public static class ItineraryItem {
//        public String compassDirection;
//        public List<Detail> details;
//        public String instruction;
//        public Point maneuverPoint;
//        public double travelDistance;
//        public double travelDuration;
//    }
//
//    public static class Detail {
//        public int compassDegrees;
//        public String maneuverType;
//        public String mode;
//        public List<String> names;
//        public String roadType;
//    }
}
