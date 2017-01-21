package org.hddm.shareroute;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;

import org.hddm.model.Route;
import org.hddm.utils.BaseUrl;
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
    Button btnDrawRoute;
    static int currentFragmentId;
    static  String userId;
    FragmentTransaction ft;
    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnDrawRoute = (Button) toolbar.findViewById(R.id.btn_draw_route);
        if(btnDrawRoute!=null)
            btnDrawRoute.setVisibility(View.GONE);

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

        } else if (id == R.id.nav_share) {

        }  else if (id == R.id.logout) {
            removeLogin();
            startActivity(new Intent(this, LoginActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void removeLogin(){
        new FcmInstanceIdRemoveFromDatabaseTask(this).execute();
        removeSharedPrefCredentials();
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
                    return true;
                }
                else{
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
    private void displayContentFragment(int id) {
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
                btnDrawRoute.setOnClickListener(this);
            }
        }
    }
}
