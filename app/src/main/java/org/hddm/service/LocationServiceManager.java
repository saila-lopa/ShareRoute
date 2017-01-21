package org.hddm.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.hddm.shareroute.R;

/**
 * Created by root on 7/26/16.
 */
public class LocationServiceManager implements LocationListener {

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 250; // 10 meters

    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000*30; //1000 * 60 * 1; // 1 minute

    private final static boolean forceNetwork = false;

    public static LocationServiceManager instance = null;

    private LocationManager locationManager;
    public Location location;
    public double longitude;
    public double latitude;
    private Context context;
    public long driverId;
    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
    public boolean isGPSEnabled,isNetworkEnabled, locationServiceAvailable;
    /**
     * Singleton implementation
     * @return
     */
    public static LocationServiceManager getLocationManager(Context context, long driverId)     {
        if (instance == null) {
            instance = new LocationServiceManager(context);
        }
        return instance;
    }

    /**
     * Local constructor
     */
    public LocationServiceManager(Context context )     {
        this.context = context;
        initLocationService(context);
//        LogService.log("LocationService created");
    }

    /**
     * Sets up location service after permissions is granted
     */
    @TargetApi(23)
    public void initLocationService(Context context) {

        if ( Build.VERSION.SDK_INT >= 23)
        {
            int FINE_GRAIN_PERMISSION = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
            int COARSE_GRAIN_PERMISSION = ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_COARSE_LOCATION);
            int GRANTED_PERMISSION = PackageManager.PERMISSION_GRANTED;
            if(FINE_GRAIN_PERMISSION !=GRANTED_PERMISSION && COARSE_GRAIN_PERMISSION!=GRANTED_PERMISSION)
            {
                this.locationServiceAvailable = false;
                return;
            }
        }
        try   {
            this.longitude = 0.0;
            this.latitude = 0.0;
            this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (forceNetwork) isGPSEnabled = false;

            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
            }
            else
            {
                this.locationServiceAvailable = true;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null)   {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                        updateCoordinates();
                    }
                }//end if
                else if (isGPSEnabled)  {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null)  {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                        updateCoordinates();
                    }
                }
                if(location!=null)
                {
                    this.longitude = location.getLongitude();
                    this.latitude = location.getLatitude();
                    SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("latitude", latitude +"");
                    editor.putString("longitude", longitude +"");
                    editor.commit();
                }
            }
        } catch (Exception ex)  {
//            LogService.log( "Error creating location service: " + ex.getMessage() );
            Log.d("LEE", ex.getMessage());

        }
    }


    @Override
    public void onLocationChanged(Location location) {
        // do stuff here with location object
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("latitude", latitude +"");
        editor.putString("longitude", longitude +"");
        editor.commit();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        this.initLocationService(context);
    }

    @Override
    public void onProviderDisabled(String provider) {
        this.initLocationService(context);
    }
}