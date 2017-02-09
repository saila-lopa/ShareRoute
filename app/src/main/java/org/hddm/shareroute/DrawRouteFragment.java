package org.hddm.shareroute;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.hddm.service.LocationServiceManager;
import org.hddm.utils.Constraints;
import org.hddm.utils.DataParser;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Saila on 12/24/2016.
 */
public class DrawRouteFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private ArrayList<LatLng> selectedPoints;
    private ArrayList<Marker> MarkerPoints;
    private static final String[] LOCATION_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
    LocationServiceManager locationManagerService;
    Context context;
    public static List<List<LatLng>>  pathPolyline;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Bundle args = getArguments();
//        mKey = args.getString(ARG_KEY);
//        mPage = mCallbacks.onGetPage(mKey);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        selectedPoints = new ArrayList<LatLng>();
        MarkerPoints = new ArrayList<Marker>();
        context= getActivity();
        return rootView;
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
//        locationManagerService = new LocationServiceManager(context);
//        if(!locationManagerService.locationServiceAvailable)
//        {
//            if ( Build.VERSION.SDK_INT >= 23) {
//                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
//            }
//            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
//            dialog.setMessage("Location service is not enabled!Please enable Location service!");
//            dialog.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                    // TODO Auto-generated method stub
//                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 100);
//                }
//            });
//                /*
//                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                        // TODO Auto-generated method stub
//
//                    }
//                }); */
//            dialog.show();
//        }
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
        String lat = sharedPref.getString("latitude", "");
        String lng = sharedPref.getString("longitude", "");
        LatLng currentPosition;
        if(lat.isEmpty() || lng.isEmpty()) {
            currentPosition = new LatLng(23.726277, 90.392379);  //default location - BUET
        } else {
            currentPosition = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        }
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        MarkerPoints.add(currentPosition);
//        mMap.addMarker(new MarkerOptions().position(currentPosition));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition,Constraints.mapZoomLevel));
        mMap.setTrafficEnabled(true);
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        selectedPoints.add(latLng);
//        // Creating a marker
//        MarkerOptions markerOptions = new MarkerOptions();
//
//        // Setting the position for the marker
//        markerOptions.position(latLng);
//
//        // Setting the title for the marker.
//        // This will be displayed on taping the marker
//        markerOptions.title(latLng.latitude + " : " + latLng.longitude);
//
//        // Animating to the touched position
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//
//        // Placing a marker on the touched position
//        mMap.addMarker(markerOptions);
//        Log.d("ADDED LATITUDE",String.valueOf(latLng.latitude));
//        Log.d("ADDED LONGITUDE",String.valueOf(latLng.longitude));
//
//        //Save it to SharedPreference or SQLite .
//        Toast.makeText(getActivity().getApplicationContext(),"Block area updated",Toast.LENGTH_LONG).show();
        // Adding new item to the ArrayList
        //MarkerPoints.add(latLng);

        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(latLng);

        /**
         * For the start location, the color of marker is GREEN and
         * for the end location, the color of marker is RED.
         */
        if (MarkerPoints.size() == 0) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if (MarkerPoints.size() == 1) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }


        // Add new marker to the Google Map Android API V2
        Marker marker= mMap.addMarker(options);
        MarkerPoints.add(marker);

        // Checks, whether start and end locations are captured
        if (MarkerPoints.size() >= 2) {
            drawNewRoute();
        }

    }
    private void drawNewRoute() {
        // Getting URL to the Google Directions API
        mMap.clear();
        String url = getUrl();
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        String[] params = {url};
        FetchUrl.execute(params);
        for (int i=0; i<MarkerPoints.size(); i++) {
            MarkerOptions option = new MarkerOptions();
            option.position(MarkerPoints.get(i).getPosition());
            mMap.addMarker(option);
        }
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        final Marker currentMarker = marker;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Do you want to delete this marker?");
        builder.setTitle("Marker Option");
        builder.setCancelable(false);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
//                for(Marker position:MarkerPoints) {
//                    if(position.getId().equalsIgnoreCase(currentMarker.getId())) {
//                        tappedMarker = position;
//                        break;
//                    }
//                }
               boolean status =  MarkerPoints.remove(currentMarker);
                currentMarker.remove();
                if(MarkerPoints.size()>=2) {
                    drawNewRoute();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        final  AlertDialog MessageModalDialog = builder.create();
        MessageModalDialog.show();
        return false;
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }
    private String getUrl() {
        StringBuilder urlString = new StringBuilder();
        if(MarkerPoints!=null && MarkerPoints.size()>=2) {
            urlString.append("https://maps.googleapis.com/maps/api/directions/json?");
            urlString.append("origin=" + MarkerPoints.get(0).getPosition().latitude + "," + MarkerPoints.get(0).getPosition().longitude);
            if(MarkerPoints.size()>2){
                urlString.append("&waypoints=" + MarkerPoints.get(1).getPosition().latitude + "," + MarkerPoints.get(1).getPosition().longitude);
                for(int i=2; i<MarkerPoints.size()-1; i++) {
                    urlString.append("|" + MarkerPoints.get(i).getPosition().latitude + "," + MarkerPoints.get(i).getPosition().longitude);
                }
            }
            int endPosition = MarkerPoints.size()-1;
            urlString.append("&destination=" + MarkerPoints.get(endPosition).getPosition().latitude + "," + MarkerPoints.get(endPosition).getPosition().longitude);
            urlString.append("&sensor=false" + "&key=" + Constraints.apiKey);
        }
        return  urlString.toString();
    }
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            if(iStream!=null)
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            pathPolyline = new ArrayList<List<LatLng>>();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                pathPolyline.add(points);
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");
            }
            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}
