package org.hddm.shareroute;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.hddm.service.LocationServiceManager;
import org.hddm.utils.Constraints;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saila on 1/27/2017.
 */
public class TrackRouteFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static final String[] LOCATION_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST = 1337;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;
    LocationServiceManager locationManagerService;
    Context context;

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
        context = getActivity();
        locationManagerService = new LocationServiceManager(context);
//        boolean service = locationManagerService.locationServiceAvailable;
//        boolean sz = locationManagerService.pointsInPath.size()>0 ? true : false;
        boolean ts = locationManagerService.trackingStatus;
        if (ts) {
            //draw already tracked path;
            List<List<LatLng>> multiLine = new ArrayList<List<LatLng>>();
            multiLine.add(locationManagerService.pointsInPath);
            drawRoute(multiLine);
        } else {

        }
        return rootView;
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
//            dialog.setMessage("Location service is not enabled!To track route please enable Location service!");
//            dialog.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                    // TODO Auto-generated method stub
//                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 100);
//                }
//            });
//            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                    // TODO Auto-generated method stub
//                    Toast.makeText(context, "Cant track route without location service being enabled!", Toast.LENGTH_LONG).show();
//                }
//            });
//            dialog.show();
//        }
//        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
//        String lat = sharedPref.getString("latitude", "");
//        String lng = sharedPref.getString("longitude", "");
//        LatLng currentPosition;
//        if (lat.isEmpty() || lng.isEmpty()) {
////            currentPosition = new LatLng(23.726277, 90.392379);  //default location - BUET
//        } else {
//            currentPosition = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, Constraints.mapZoomLevel));
//            mMap.setTrafficEnabled(true);
//        }
//         Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
//        MarkerPoints.add(currentPosition);
        mMap.addMarker(new MarkerOptions().position(sydney));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, Constraints.mapZoomLevel));
//            mMap.setTrafficEnabled(true);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (locationManagerService.locationServiceAvailable) {
                //change the button text to "Stop Tracking"
            } else {
                Toast.makeText(context, "Can't track route without location service being enabled!", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void drawRoute(List<List<LatLng>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        // Traversing through all the routes
        if(result!=null) {
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
}
