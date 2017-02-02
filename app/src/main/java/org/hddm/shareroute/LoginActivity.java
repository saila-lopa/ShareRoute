package org.hddm.shareroute;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.hddm.utils.*;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity implements View.OnClickListener {

    private Boolean exit = false;
    String message = "";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //ActionBar bar = getActionBar();
        //bar.hide();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //getActionBar().hide();
        Button btnlogin = (Button) findViewById(R.id.btnSubmit);
        btnlogin.setOnClickListener(this);

        TextView txtforgotpass = (TextView) findViewById(R.id.txtsignup);
        txtforgotpass.setOnClickListener(this);

        getActionBar().hide();

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.banner);
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Log.d("LEE", width + "");
            Log.d("LEE", height + "");
            double ratio = (double) height / (double) width;

            DisplayMetrics displaymetrics = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            double screenWidth = (int) displaymetrics.widthPixels;

            double imagewidth = screenWidth * 0.4;

            double imageheight = imagewidth * ratio;
            ImageView logo = (ImageView) findViewById(R.id.app_logo);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) imageheight);
            logo.setLayoutParams(rlp);
            logo.setImageBitmap(bitmap);
        }

    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
            //System.exit(0);
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
       if (v.getId() == R.id.btnSubmit) {
            /*
            Intent intent = new Intent(MainActivity.this, OrderListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			*/
            EditText etEmail = (EditText) findViewById(R.id.etEmail);
            EditText etPass = (EditText) findViewById(R.id.etPass);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
            String email = etEmail.getText().toString();
            String pass = etPass.getText().toString();
            if (!email.isEmpty() && !pass.isEmpty()) {
                boolean isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
                if (isValidEmail) {
                    String Url = BaseUrl.HTTP + "login";
                    Log.d("URL", Url);
                    JSONObject mJsonObject = JsonHelper.getLoginJson(email, pass);
                    makeServerRequest(Url, mJsonObject);

                } else {
                    //say invalid email
					/*Toast.makeText(this, "Invalid Emial", Toast.LENGTH_LONG).show();
					TextView errormsg = (TextView) findViewById(R.id.errormsg);
					errormsg.setText("Invalid Emial");
					errormsg.setTextColor(android.graphics.Color.RED);
					errormsg.setVisibility(View.VISIBLE);*/
					etEmail.setError("Invalid e-mail address");
                }
            } else if (email.isEmpty())
                etEmail.setError("Required");
            else if (pass.isEmpty())
                etPass.setError("Required");
        } else if(v.getId() == R.id.txtsignup) {
           Intent ii = new Intent(LoginActivity.this, RegistrationActivity.class);
           startActivity(ii);
       }
    }

    @SuppressWarnings("unchecked")
    private void makeServerRequest(final String url_base, final JSONObject jSONObject) {
        new LoginTask(url_base, jSONObject).execute();
    }

    class LoginTask extends AsyncTask<Void, Void, JSONObject> {

        String url_base;
        JSONObject obj;

        public LoginTask(String url_base, JSONObject obj) {
            // TODO Auto-generated constructor stub
            this.url_base = url_base;
            this.obj = obj;
        }

        private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //dialog.setMessage("Loading...");

            dialog.setCancelable(false);
            //TextView txtvw = new TextView(getActivity());
            dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                Log.d("LEE", "*" + url_base + "*");
                String results = HttpJsonPost.POST(url_base, obj);
                //Toast.makeText(getApplicationContext(), "TOast" +results,Toast.LENGTH_LONG).show();
                //Log.d("LEE", "Results : " + results);
                final JSONObject json_ob = new JSONObject(results);
                message = json_ob.getString("message");
                //Toast.makeText(getApplicationContext(), "Toast " + message,Toast.LENGTH_LONG).show();
                return json_ob;
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
        protected void onPostExecute(JSONObject result) {
            //upload image link in server database
            super.onPostExecute(result);

            JSONObject js_ob = result;
            if (message.equalsIgnoreCase("logged in successfully.")) {
                result = js_ob;
                //String tmo = "abc";
                try {
                    JSONObject user = result.getJSONObject("user");
                    SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("userId", user.getString("userId"));
                    editor.putString("email", user.getString("email"));
                    editor.putString("name", user.getString("name"));
                    editor.commit();
                    FirebaseInstanceId.getInstance().getToken();
                    Intent ii = new Intent(context, MainActivity.class);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(ii);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (message.equalsIgnoreCase("no match found")) {
                Toast.makeText(getApplicationContext(), "Invalid email password", Toast.LENGTH_LONG).show();
            } else if (message.equalsIgnoreCase("")) {
                Toast.makeText(getApplicationContext(), "Server Connection problem .please try again.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Unknown error occured .please try again.", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();

        }
    }

}
