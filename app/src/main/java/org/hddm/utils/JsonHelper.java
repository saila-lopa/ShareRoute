package org.hddm.utils;

import com.google.android.gms.maps.model.LatLng;

import org.hddm.model.Route;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonHelper {

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
