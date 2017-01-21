package org.hddm.shareroute;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.hddm.utils.Constraints;

/**
 * Created by Saila on 12/18/2016.
 */
public class RouteViewActivity extends Activity implements OnMapReadyCallback{
    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_view);
//        SupportMapFragment mapFragment = (SupportMapFragment) findViewById(R.id.map);
//        mapFragment.getMapAsync(this);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, Constraints.mapZoomLevel));
        mMap.setTrafficEnabled(true);
    }
}
