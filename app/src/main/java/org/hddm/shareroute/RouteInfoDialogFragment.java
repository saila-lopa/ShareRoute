package org.hddm.shareroute;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.hddm.model.Route;
import org.hddm.utils.BaseUrl;
import org.hddm.utils.HttpJsonPost;
import org.hddm.utils.JsonHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Saila on 1/20/2017.
 */
public class RouteInfoDialogFragment extends DialogFragment {

    private EditText  routNameEt, fareEt, rideEt, noteEt, qualityEt;

    public RouteInfoDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static RouteInfoDialogFragment newInstance(String title) {
        RouteInfoDialogFragment frag = new RouteInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_info, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        routNameEt = (EditText) view.findViewById(R.id.et_route_name);
        fareEt = (EditText) view.findViewById(R.id.et_fare);
        rideEt = (EditText) view.findViewById(R.id.et_ride);
        noteEt = (EditText) view.findViewById(R.id.et_note);
        qualityEt = (EditText) view.findViewById(R.id.et_quality);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        routNameEt.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        Button btnSave = (Button) view.findViewById(R.id.btn_save);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRoute();
            }
        });
    }
    private void saveRoute() {
        String routeName = routNameEt.getText().toString();
        String fare = fareEt.getText().toString();
        String ride = rideEt.getText().toString();
        String note = noteEt.getText().toString();
        String quality = qualityEt.getText().toString();
        Route route = new Route();
        route.setRouteName(routeName);
        route.setFare(fare);
        route.setRide(ride);
        route.setNote(note);
        route.setQuality(quality);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
        String userId = sharedPref.getString("userId", "");
        route.setCreatedBy(userId);

        List<List<LatLng>> points;
        if(MainActivity.currentFragmentId == R.id.draw_route ) {
            route.setCreatedThrough(MainActivity.currentFragmentId);
            points = DrawRouteFragment.pathPolyline;
            route.setPointsOnPath(points);
        } else if(MainActivity.currentFragmentId == R.id.track_route) {
            route.setCreatedThrough(MainActivity.currentFragmentId);
            points = DrawRouteFragment.pathPolyline;
            route.setPointsOnPath(points);
        }
        new saveRouteTask(route).execute();
    }
    class saveRouteTask extends AsyncTask<Void ,Void, String> {
        Route route;
        public saveRouteTask(Route route) {
            // TODO Auto-generated constructor stub
            this.route = route;
        }
        private ProgressDialog dialog = new ProgressDialog(getActivity());
        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Saving...");

            dialog.setCancelable(false);
//            TextView txtvw = new TextView(getActivity());
            dialog.show();
        }
        @Override
        protected  String doInBackground(Void... params) {
            try {
                JSONObject routeJson = JsonHelper.getRouteJson(route);
                String url = BaseUrl.HTTP + "save-path";
                String results = HttpJsonPost.POST(url, routeJson);
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
            dialog.dismiss();
            if(results!=null && !results.equals("network error")) {
                try {
                    JSONObject jsonOb = new JSONObject(results);
                    if(jsonOb.has("routeId") && jsonOb.getString("routeId")!=null) {
                        getDialog().dismiss();
                        Toast.makeText(getActivity(), "Route saved successfully!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
