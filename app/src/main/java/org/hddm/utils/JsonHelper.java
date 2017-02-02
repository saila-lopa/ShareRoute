package org.hddm.utils;

import com.google.android.gms.maps.model.LatLng;

import org.hddm.model.Route;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonHelper {

    public static Route parseRouteDetails(String routeString) {
        try {
            JSONObject routeJSon = new JSONObject(routeString);
            JSONObject jsonRouteObj = routeJSon.getJSONObject("route");
            Route route = new Route();
            route.setRouteId(jsonRouteObj.getString("routeId"));
            if (jsonRouteObj.has("routeName"))
                route.setRouteName(jsonRouteObj.getString("routeName"));
            if (jsonRouteObj.has("quality"))
                route.setQuality(jsonRouteObj.getString("quality"));
            if (jsonRouteObj.has("note"))
                route.setNote(jsonRouteObj.getString("note"));
            if (jsonRouteObj.has("ride"))
                route.setRide(jsonRouteObj.getString("ride"));
            if (jsonRouteObj.has("fare"))
                route.setFare(jsonRouteObj.getString("fare"));
            if (jsonRouteObj.has("createDate"))
                route.setCreateDate(jsonRouteObj.getString("createDate"));
            if(jsonRouteObj.has("multilineString")) {
                String multilineString = jsonRouteObj.getString("multilineString");
                List<List<LatLng>> multiLines = parseMultiLineString(multilineString);
                route.setPointsOnPath(multiLines);
            }
            return route;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }
    public static List<List<LatLng>> parseMultiLineString(String multiLineString) {
        List<List<LatLng>> polylinePoints = new ArrayList<List<LatLng>>();
        String[] tmp = multiLineString.split("\\(");
        if(tmp.length>=3) {
            for (int i=2; i<tmp.length; i++) {
                String[] pointsInLine = tmp[i].split(",");
                List<LatLng> line = new ArrayList<LatLng>();
                for (int j=0; j<pointsInLine.length; j++) {
                    String[] points = pointsInLine[j].split(" ");
                    points[0] = points[0].replaceAll("[^\\d.]", "");
                    points[1] = points[1].replaceAll("[^\\d.]", "");
                    double latitude = Double.parseDouble(points[0]);
                    double longitude = Double.parseDouble(points[1]);
                    LatLng latlng = new LatLng(latitude, longitude);
                    line.add(latlng);
                }
                polylinePoints.add(line);
            }
        }
        String[] tmp2 = tmp[1].split("\\(");
        return  polylinePoints;
    }
    public static List<String> parseUserSuggestion(JSONObject usersJson) {
        List<String> users = new ArrayList<String>();
        int k=0;
        try {
            JSONArray usersJSONArray = usersJson.getJSONArray("userList");
            int i;
            for (i = 0; i < usersJSONArray.length(); i++) {
                JSONObject jsonUserObj = usersJSONArray.getJSONObject(i);
                if (jsonUserObj.has("email"))
                    users.add(jsonUserObj.getString("email"));
            }
            return users;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  users;
    }
    public static List<Route> parseRouteList(JSONObject routeListJSon) {
        List<Route> routeList = new ArrayList<Route>();
        try {
            JSONArray routes = routeListJSon.getJSONArray("routeList");
            int i;
            for (i = 0; i < routes.length(); i++) {
                JSONObject jsonRouteObj = routes.getJSONObject(i);
                Route route = new Route();
                route.setRouteId(jsonRouteObj.getString("routeId"));
                if (jsonRouteObj.has("routeName"))
                    route.setRouteName(jsonRouteObj.getString("routeName"));
                if (jsonRouteObj.has("quality"))
                    route.setQuality(jsonRouteObj.getString("quality"));
                if (jsonRouteObj.has("note"))
                    route.setNote(jsonRouteObj.getString("note"));
                if (jsonRouteObj.has("ride"))
                    route.setRide(jsonRouteObj.getString("ride"));
                if (jsonRouteObj.has("fare"))
                    route.setFare(jsonRouteObj.getString("fare"));
                if (jsonRouteObj.has("createDate"))
                    route.setCreateDate(jsonRouteObj.getString("createDate"));
                routeList.add(route);
            }
            return routeList;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  routeList;
    }

    public static JSONObject getRouteJson(Route route) {
        JSONObject finalobject = new JSONObject();
        try {
            JSONObject json = new JSONObject();
            json.put("routeName", "" + route.getRouteName());
            json.put("note", "" + route.getNote());
            json.put("ride", "" + route.getRide());
            json.put("fare", "" + route.getFare());
            json.put("quality", "" + route.getQuality());
            json.put("createdBy", "" + route.getCreatedBy());
            json.put("createdThrough", "" + route.getCreatedThrough());
            List<List<LatLng>> pointsInPath = route.getPointsOnPath();
            StringBuilder multilineString = new StringBuilder();
            if(pointsInPath!=null && pointsInPath.size()>0) {
                multilineString.append("MULTILINESTRING(");
                String lineString = formLineString(pointsInPath.get(0));
                multilineString.append(lineString);
                for (int i=1; i<pointsInPath.size(); i++) {
                    multilineString.append(", ");
                    lineString = formLineString(pointsInPath.get(i));
                    multilineString.append(lineString);
                }
                multilineString.append(")");
                json.put("multilineString", multilineString);
            }
            finalobject = json;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return finalobject;
    }
    private static String formLineString(List<LatLng> line) {
        StringBuilder lineString = new StringBuilder();
        lineString.append("(");
        lineString.append(line.get(0).latitude + " " + line.get(0).longitude);
        for(int i=1; i<line.size();i++) {
            lineString.append(", " + line.get(i).latitude + " " + line.get(i).longitude);
        }
        lineString.append(")");
        return lineString.toString();
    }
    public static JSONObject getRegisterJSON(String name, String email, String password) {
        JSONObject finalobject = new JSONObject();
        try {
            JSONObject json = new JSONObject();
            json.put("name", "" + name);
            json.put("email", "" + email);
            json.put("password", "" + password);

            //finalobject.put("data", json);
            //finalobject.put("token", "");


            finalobject = json;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return finalobject;
    }
    public static JSONObject getLoginJson(String email, String password) {

        JSONObject finalobject = new JSONObject();
        try {
            JSONObject json = new JSONObject();
            json.put("email", "" + email);
            json.put("password", "" + password);

            //finalobject.put("data", json);
            //finalobject.put("token", "");


            finalobject = json;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return finalobject;
    }
}
