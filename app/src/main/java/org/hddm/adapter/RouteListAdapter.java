package org.hddm.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.hddm.model.Route;
import org.hddm.shareroute.MainActivity;
import org.hddm.shareroute.R;
import org.hddm.shareroute.RouteInfoDialogFragment;
import org.hddm.shareroute.RouteViewActivity;
import org.hddm.utils.BaseUrl;
import org.hddm.utils.DataParser;
import org.hddm.utils.HttpJsonPost;
import org.hddm.utils.JsonHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Saila on 12/18/2016.
 */
public class RouteListAdapter extends BaseAdapter implements View.OnClickListener{

    Context context;
    String prevActivity;
    List<Route> routeList;
    static  ArrayAdapter<String> userListAdapter;
    private static List<String> users;
    private static  List<String> newUsers;
    public RouteListAdapter(Context con, List<Route> routeList)
    {
        this.routeList = routeList;
        context = con;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return routeList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        // ViewHolder holder ;
//        if (convertView == null)
//        {
            gridView = inflater.inflate( R.layout.grid_route_list , null);
//        }
//        else
//        {
//            //holder = (ViewHolder) convertView.getTag();
//            gridView = (View) convertView;
//        }
        TextView routeNameTv = (TextView) gridView.findViewById(R.id.route_name);
        TextView timeTv = (TextView) gridView.findViewById(R.id.time);
        TextView noteTv = (TextView) gridView.findViewById(R.id.note);
        TextView rideTv = (TextView) gridView.findViewById(R.id.ride);
        TextView fareTv = (TextView) gridView.findViewById(R.id.fare);
        final Route route = routeList.get(position);
        routeNameTv.setText(route.getRouteName());
        timeTv.setText("Estimated/User recommended Time: " + route.getQuality());
        noteTv.setText(route.getNote());
        rideTv.setText(route.getRide());
        fareTv.setText(route.getFare());

        Button btnShare = (Button)gridView.findViewById(R.id.btn_share);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()== R.id.btn_share) {
                    UserSelectDialog userSelectDialog=new UserSelectDialog(context, route.getRouteId());
                    userSelectDialog.show();
                }
            }
        });
        int routeId = Integer.parseInt(route.getRouteId());
        gridView.setId(routeId);
        gridView.setOnClickListener(this);
        return gridView;
    }
    public class UserSelectDialog extends Dialog implements
            android.view.View.OnClickListener {

        public Context context;
        public Dialog d;
        public Button btnCancel, btnShare;
        String routeId;

        public UserSelectDialog(Context context, String routeId) {
            super(context);
            // TODO Auto-generated constructor stub
            this.context = context;
            this.routeId = routeId;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_select_user);

            users = new ArrayList<String>();
            users.add("test@email.com");
            users.add("test2@email.com");
            users.add("test3@email.com");
            users.add("test4@email.com");
            userListAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_dropdown_item_1line, users);
            final AutoCompleteTextView textView = (AutoCompleteTextView)
                    findViewById(R.id.search_user);
            textView.setAdapter(userListAdapter);
            textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                }
            });
            textView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String userText = textView.getText().toString();
                    if(!userText.isEmpty()) {
                        new FetchUserSearchSuggestionTask(userText).execute();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            btnCancel = (Button) findViewById(R.id.btn_cancel);
            btnShare = (Button) findViewById(R.id.btn_share);
            btnCancel.setOnClickListener(this);
            btnShare.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_cancel:
                    dismiss();
                    break;
                case R.id.btn_share:
                    AutoCompleteTextView textView = (AutoCompleteTextView)
                            findViewById(R.id.search_user);
                    String selectedEmail = textView.getText().toString();
                    SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
                    String userId = sharedPref.getString("userId", "");
                    String userName = sharedPref.getString("name", "");
                    new SendRouteToUserTask(userId, userName, selectedEmail, routeId).execute();
                    break;
                default:
                    break;
            }
            dismiss();
        }
    }
    class SendRouteToUserTask extends AsyncTask<Void, Void, String> {

        String userEmail, routeId, sharedBy, userName;

        public SendRouteToUserTask(String sharedBy, String userName, String userEmail, String routeId) {
            // TODO Auto-generated constructor stub
            this.userEmail = userEmail;
            this.routeId = routeId;
            this.sharedBy = sharedBy;
            this.userName = userName;
        }

        private ProgressDialog dialog = new ProgressDialog(context);

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Sending route...");

            dialog.setCancelable(false);
//            TextView txtvw = new TextView(getActivity());
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject json = new JSONObject();
                json.put("userEmail", "" + this.userEmail);
                json.put("routeId", "" + this.routeId);
                json.put("sharedBy", "" + this.sharedBy);
                json.put("userName", "" + this.userName);
                String url = BaseUrl.HTTP + "share-route";
                String results = HttpJsonPost.POST(url, json);
                Log.d("LEE", "Results : " + results);
                if (results == null || results.equals("network error")) {

                } else {
                    try {
                        JSONObject json_ob = new JSONObject(results);
                        newUsers = JsonHelper.parseUserSuggestion(json_ob);
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
            if (results == null || results.equals("network error")) {

            } else {
                try {
                    json_ob = new JSONObject(results);
                    if(json_ob.has("message") && json_ob.getString("message").equals("shared")) {
                        Toast.makeText(context, "Route sent to user!", Toast.LENGTH_LONG).show();
                        context.startActivity(new Intent(context, MainActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            MainActivity.this.mHandler = new Handler();
//            m_Runnable.run();
            //dialog.dismiss();
        }
    }
    class FetchUserSearchSuggestionTask extends AsyncTask<Void, Void, String> {

        String searchText;

        public FetchUserSearchSuggestionTask(String searchText) {
            // TODO Auto-generated constructor stub
            this.searchText = searchText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject json = new JSONObject();
                json.put("searchText", "" + this.searchText);
                String url = BaseUrl.HTTP + "search-user";
                String results = HttpJsonPost.POST(url, json);
                Log.d("LEE", "Results : " + results);
                if (results == null || results.equals("network error")) {

                } else {
                    JSONObject json_ob = new JSONObject(results);
                    newUsers = JsonHelper.parseUserSuggestion(json_ob);
                }
                if (results.isEmpty())
                    return null;
                return results;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String results) {
            super.onPostExecute(results);
//            dialog.dismiss();
            JSONObject json_ob;
            if (results == null || results.equals("network error")) {

            } else {
                if (newUsers!= null && newUsers.size()>0) {
                    users = newUsers;
                    userListAdapter.notifyDataSetChanged();
                }
            }
//            MainActivity.this.mHandler = new Handler();
//            m_Runnable.run();
            //dialog.dismiss();
        }
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent ii =new Intent(context, RouteViewActivity.class);
        ii.putExtra("routeId", v.getId());
        context.startActivity(ii);
    }

    }
