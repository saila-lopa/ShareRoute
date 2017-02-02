package org.hddm.shareroute;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import org.hddm.model.Route;
import org.hddm.service.LocationServiceManager;
import org.hddm.utils.BaseUrl;
import org.hddm.utils.Constraints;
import org.hddm.utils.HttpJsonPost;
import org.hddm.utils.JsonHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Saila on 12/17/2016.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{
    Toolbar toolbar;
    Button btnDrawRoute, btnTrackRoute;
    static int currentFragmentId;
    static  String userId;
    FragmentTransaction ft;
    Context context = this;
    LocationServiceManager locationManagerService;
    private static final String[] LOCATION_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnDrawRoute = (Button) toolbar.findViewById(R.id.btn_draw_route);
        btnTrackRoute = (Button) toolbar.findViewById(R.id.btn_track_route);
        if(btnDrawRoute!=null)
            btnDrawRoute.setVisibility(View.GONE);
        if(btnTrackRoute!=null)
            btnTrackRoute.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displayContentFragment(R.id.route_list);
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");
        if(userId.isEmpty()) {
            Intent ii = new Intent(MainActivity.this, LoginActivity.class);
            ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(ii);
        } else {
            String fcmToken = sharedPref.getString("fcmToken","");
            if(!fcmToken.isEmpty() && fcmToken.length()>0) {
                new SendFCMTokenTask(userId, fcmToken).execute();
            }
        }
    }
    class SendFCMTokenTask extends AsyncTask<Void ,Void, String> {
        String fcmToken;
        String userId;
        public SendFCMTokenTask(String userId, String fcmToken) {
            // TODO Auto-generated constructor stub
            this.fcmToken = fcmToken;
            this.userId = userId;
        }
        @Override
        protected  String doInBackground(Void... params) {
            try {
                JSONObject json = new JSONObject();
                json.put("userId", "" + userId);
                json.put("fcmToken", this.fcmToken);
                String url = BaseUrl.HTTP + "save-fcm-token";
                String results = HttpJsonPost.POST(url, json);
                Log.d("LEE", "Results : " + results);
                return results;
            } catch (ExceptionInInitializerError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return  null;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params) {

        }

        @Override
        protected void onPostExecute( String results) {
            super.onPostExecute(results);
            if(results!=null && !results.equals("network error")) {
                try {
                    JSONObject jsonOb = new JSONObject(results);
                    if(jsonOb.has("userId") && jsonOb.getString("userId")!=null) {
                        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("fcmToken","");
                        editor.putString("notificationToken",jsonOb.getString("fcmToken"));
                        editor.commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_draw_route) {
            showEditDialog();
        } else if(v.getId() == R.id.btn_track_route) {
            String trackRouteBtnText = btnTrackRoute.getText().toString();
            if(trackRouteBtnText.equalsIgnoreCase("start tracking")) {
                startTracking();
            } else {
                manageTrackingStop();
            }
        }
    }
    private void startTracking() {
        locationManagerService = new LocationServiceManager(context);
        if(!locationManagerService.locationServiceAvailable)
        {
            if ( Build.VERSION.SDK_INT >= 23) {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage("Location service is not enabled!To track route please enable Location service!");
            dialog.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 100);
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Toast.makeText(context, "Cant track route without location service being enabled!", Toast.LENGTH_LONG).show();
                }
            });
            dialog.show();
        } else {
            manageTrackingStart();
        }
    }
    private void manageTrackingStart() {
        btnTrackRoute.setText("Stop Tracking");
        locationManagerService.trackingStatus = true;
//        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
//        String lat = sharedPref.getString("latitude", "");
//        String lng = sharedPref.getString("longitude", "");
//        LatLng currentPosition;
//        if(lat.isEmpty() || lng.isEmpty()) {
//
//        } else {
//            currentPosition = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
//        }
    }
    private void manageTrackingStop() {
        locationManagerService.trackingStatus = false;
        btnTrackRoute.setText("Stopped Tracking");
        showEditDialog();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 100){
            if(locationManagerService.locationServiceAvailable) {
                //change the button text to "Stop Tracking"
                manageTrackingStart();
            }else {
                Toast.makeText(context, "Cant track route without location service being enabled!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        RouteInfoDialogFragment editNameDialogFragment = RouteInfoDialogFragment.newInstance("Additional Info");
        editNameDialogFragment.show(fm, "fragment_edit_name");
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.route_list) {
            displayContentFragment(id);
        } else if (id == R.id.draw_route) {
            displayContentFragment(id);
        } else if (id == R.id.track_route) {
            displayContentFragment(R.id.track_route);
        } else if (id == R.id.nav_share) {

        } else if(id == R.id.shared_with_me) {
            displayContentFragment(R.id.shared_with_me);
        } else if (id == R.id.logout) {
            removeLogin();
            startActivity(new Intent(this, LoginActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void removeLogin(){
        new FcmInstanceIdRemoveFromDatabaseTask(this).execute();
    }
    public class FcmInstanceIdRemoveFromDatabaseTask extends AsyncTask<Void ,Void, Boolean> {
        String message;
        Context context;

        public FcmInstanceIdRemoveFromDatabaseTask(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
                String Url = BaseUrl.HTTP + "delete-fcm-token";
                SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
                String userId = sharedPref.getString("userId", "");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", userId);
                jsonObject.put("fcmToken", sharedPref.getString("notificationToken",""));
                String results = HttpJsonPost.POST(Url, jsonObject);
                //Toast.makeText(getApplicationContext(), "TOast" +results,Toast.LENGTH_LONG).show();
                //Log.d("LEE", "Results : " + results);
                final JSONObject json_ob = new JSONObject(results);
                message = json_ob.getString("message");
                //Toast.makeText(getApplicationContext(), "Toast " + message,Toast.LENGTH_LONG).show();
                if (message!=null  &&  message.equalsIgnoreCase("token updated")){
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("notificationToken", null);
                    editor.commit();
                    removeSharedPrefCredentials();
                    return true;
                }
                else{
                    removeSharedPrefCredentials();
                    return false;
                }
            } catch (ExceptionInInitializerError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return  false;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Void... params) {

        }

    }
    private void removeSharedPrefCredentials() {
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userId", null);
        editor.putString("email", null);
        editor.putString("name", null);
        editor.commit();
    }
    public void displayContentFragment(int id) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (id) {
            case R.id.route_list:
                fragment = new RouteListFragment();
                title  = "Routes";
                break;
            case R.id.draw_route:
                fragment = new DrawRouteFragment();
                title = "Draw Route";
                break;
            case R.id.shared_with_me:
                fragment = new SharedRouteListFragment();
                title = "Draw Route";
                break;
            case R.id.track_route:
                fragment = new TrackRouteFragment();
                title = "Track Route";
                break;
        }
        if (fragment != null) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment,fragment.getClass().toString());
            ft.commit();
            adjustActionBarContent(id);
        }
        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        currentFragmentId = id;
    }
    private void adjustActionBarContent(int id) {
        if(id == R.id.draw_route) {
            if(btnDrawRoute!=null) {
                btnDrawRoute.setVisibility(View.VISIBLE);
                btnTrackRoute.setVisibility(View.GONE);
                btnDrawRoute.setOnClickListener(this);
            }
        } else if(id == R.id.track_route) {
            btnTrackRoute.setVisibility(View.VISIBLE);
            btnTrackRoute.setOnClickListener(this);
            btnDrawRoute.setVisibility(View.GONE);
        }
    }
}
