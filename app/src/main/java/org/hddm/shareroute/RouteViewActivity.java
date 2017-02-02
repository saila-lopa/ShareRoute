package org.hddm.shareroute;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;

import org.hddm.model.Route;
import org.hddm.utils.BaseUrl;
import org.hddm.utils.Constraints;
import org.hddm.utils.HttpJsonPost;
import org.hddm.utils.JsonHelper;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Saila on 12/18/2016.
 */
public class RouteViewActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    int routeId=0;
    Route route;
    LinearLayout routeHolder;
    TextView errorTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_view);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(getIntent()!=null && getIntent().getExtras()!=null)
            routeId = getIntent().getExtras().getInt("routeId");
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15));
        mMap.setTrafficEnabled(true);
        if(routeId>0)
            new RetrieveRouteDetailsTask(routeId).execute();
    }
    class RetrieveRouteDetailsTask extends AsyncTask<Void, Void, String> {

        int routeId;
        public RetrieveRouteDetailsTask(int routeId) {
            // TODO Auto-generated constructor stub
            this.routeId = routeId;
        }

        private ProgressDialog dialog = new ProgressDialog(RouteViewActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Loading...");

            dialog.setCancelable(false);
//            TextView txtvw = new TextView(getActivity());
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String url_base = BaseUrl.HTTP + "get-route";
                String results;
                JSONObject obj = new JSONObject();
                obj.put("routeId", routeId);
                results = HttpJsonPost.POST(url_base, obj);
                if (results!=null && !results.isEmpty()) {
                    route = JsonHelper.parseRouteDetails(results);
                }
                return results;
            } catch (ExceptionInInitializerError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params) {

        }

        @Override
        protected void onPostExecute(String result) {
            //upload image link in server database
            super.onPostExecute(result);
            routeHolder = (LinearLayout) findViewById(R.id.route_holder) ;
            errorTv = (TextView)findViewById(R.id.not_found_msg);
            if (result!=null && !result.isEmpty()) {
                if(route!=null) {
                    routeHolder.setVisibility(View.VISIBLE);
                    errorTv.setVisibility(View.GONE);
                    loadRouteDetails();
                }else {
                    errorTv.setVisibility(View.VISIBLE);
                    routeHolder.setVisibility(View.GONE);
                    errorTv.setText("Route not found");
                }
            } else {
                errorTv.setVisibility(View.VISIBLE);
                routeHolder.setVisibility(View.GONE);
                errorTv.setText("Route not found");
            }
            dialog.dismiss();
        }
    }
    private void loadRouteDetails() {
        if(route!=null) {
            TextView routeNameTv = (TextView) findViewById(R.id.route_name);
            TextView timeTv = (TextView) findViewById(R.id.time);
            TextView noteTv = (TextView) findViewById(R.id.note);
            TextView rideTv = (TextView) findViewById(R.id.ride);
            TextView fareTv = (TextView) findViewById(R.id.fare);
            routeNameTv.setText(route.getRouteName());
            timeTv.setText(route.getQuality());
            noteTv.setText(route.getNote());
            rideTv.setText(route.getRide());
            fareTv.setText(route.getFare());
            drawRoute(route.getPointsOnPath());
        }
    }
    private void drawRoute(List<List<LatLng>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<LatLng> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                LatLng point = path.get(j);
                points.add(point);
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.RED);
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
