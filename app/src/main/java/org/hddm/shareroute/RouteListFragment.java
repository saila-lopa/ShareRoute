package org.hddm.shareroute;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import org.hddm.adapter.RouteListAdapter;
import org.hddm.model.Route;
import org.hddm.utils.BaseUrl;
import org.hddm.utils.HttpJsonPost;
import org.hddm.utils.JsonHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saila on 12/22/2016.
 */
public class RouteListFragment extends Fragment {
    Context context;
    List<Route> routeList;
    View rootView;
    String userId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
//        Bundle args = getArguments();
//        mKey = args.getString(ARG_KEY);
//        mPage = mCallbacks.onGetPage(mKey);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_route_list, container, false);
        SharedPreferences sharedPref = context.getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");
        new RetriveRouteList(userId).execute();
        return rootView;
    }
    class RetriveRouteList extends AsyncTask<Void, Void, String> {

        String userId;

        public RetriveRouteList(String userId) {
            // TODO Auto-generated constructor stub
            this.userId = userId;
        }

        private ProgressDialog dialog = new ProgressDialog(context);

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
                JSONObject json = new JSONObject();
                json.put("userId", "" + this.userId);
                String url = BaseUrl.HTTP + "get-route-list";
                String results = HttpJsonPost.POST(url, json);
                Log.d("LEE", "Results : " + results);
                if (results == null || results.equals("network error")) {

                } else {
                    try {
                        JSONObject json_ob = new JSONObject(results);
                        routeList = JsonHelper.parseRouteList(json_ob);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (results.isEmpty())
                    return null;
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
        protected void onPostExecute(String results) {
            super.onPostExecute(results);
            dialog.dismiss();
            JSONObject json_ob;
            GridView routeGrid = (GridView) rootView.findViewById(R.id.grid_route_list);
            TextView nettext = (TextView) rootView.findViewById(R.id.networkerrortext);
            if (results == null || results.equals("network error")) {
                nettext.setVisibility(View.VISIBLE);
                routeGrid.setVisibility(View.GONE);
                nettext.setText("Oops! Couldn't connect to the internet");
            } else {
                if (routeList == null || routeList.size() <= 0) {
                    nettext.setVisibility(View.VISIBLE);
                    routeGrid.setVisibility(View.GONE);
                    nettext.setText("No Saved Route yet!");
                } else {
                    nettext.setVisibility(View.GONE);
                    routeGrid.setVisibility(View.VISIBLE);
                    RouteListAdapter gridAdapter = new RouteListAdapter(context, routeList);
                    routeGrid.setAdapter(gridAdapter);
                    gridAdapter.notifyDataSetChanged();
                }
            }
//            MainActivity.this.mHandler = new Handler();
//            m_Runnable.run();
            //dialog.dismiss();
        }
    }
}
