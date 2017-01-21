package org.hddm.shareroute;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.hddm.utils.BaseUrl;
import org.hddm.utils.HttpJsonPost;
import org.hddm.utils.JsonHelper;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Saila on 12/17/2016.
 */
public class RegistrationActivity extends Activity implements View.OnClickListener {

    private Boolean exit = false;
    String message = "";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //ActionBar bar = getActionBar();
        //bar.hide();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //getActionBar().hide();
        Button btnlogin = (Button) findViewById(R.id.btnSubmit);
        btnlogin.setOnClickListener(this);

        TextView txtforgotpass = (TextView) findViewById(R.id.txtlogin);
        txtforgotpass.setOnClickListener(this);

        getActionBar().hide();

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
            EditText etName = (EditText) findViewById(R.id.etName);
            EditText etEmail = (EditText) findViewById(R.id.etEmail);
            EditText etPass = (EditText) findViewById(R.id.etPass);
            EditText etcnfPass = (EditText) findViewById(R.id.etConfirmPass);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String pass = etPass.getText().toString();
            //Add validation staff
            String confPass = etcnfPass.getText().toString();
            if(name.length()!=0 && email.length()!=0 && pass.length()!=0 && confPass.length()!=0) {
                if(!pass.equals(confPass)) {
                    TextView errormsg = (TextView) findViewById(R.id.errormsg);
                    errormsg.setText("Password didn't match");
                    errormsg.setTextColor(android.graphics.Color.RED);
                    errormsg.setVisibility(View.VISIBLE);
                } else {
                    boolean isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
                    if(isValidEmail) {
                        new UserRegistrationTask(name, email, pass).execute();
                    } else {
                        TextView errormsg = (TextView) findViewById(R.id.errormsg);
                        errormsg.setText("Invalid e-mail address");
                        errormsg.setTextColor(android.graphics.Color.RED);
                        errormsg.setVisibility(View.VISIBLE);
                    }
                }
            } else if(name.length()==0) {
                etName.setError("Required");
            } else if(email.length()==0) {
                etEmail.setError("Required");
            } else if(pass.length()==0) {
                etPass.setError("Required");
            } else if(confPass.length()==0) {
                etcnfPass.setError("Required");
            }
        } else if(v.getId() == R.id.txtlogin) {
            Intent ii = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(ii);
        }
    }
    class UserRegistrationTask extends AsyncTask<Void, Void, JSONObject>
    {
        String Name, Email, Password;
        long UserId;
        public UserRegistrationTask(String name, String email, String password)
        {
            Name = name;
            Email = email;
            Password = password;
        }
        private ProgressDialog dialog = new ProgressDialog(RegistrationActivity.this);
        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //dialog.setMessage("Uploading, please wait.");
            dialog.setCancelable(false);
            dialog.show();
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                String url_base = BaseUrl.HTTP + "register";
                Log.d("LEE", "*" + url_base + "*");
                JSONObject registerJSON = JsonHelper.getRegisterJSON(Name, Email, Password);
                String results = HttpJsonPost.POST(url_base, registerJSON);
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
        protected void onPostExecute(JSONObject result) {
            //upload image link in server database
            JSONObject js_ob = result;
            if (message!=null  && message.equalsIgnoreCase("registered successfully.")) {
                result = js_ob;
                //String tmo = "abc";
                try {
                    JSONObject user = result.getJSONObject("user");
                    SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    String token = user.getString("token");
                    editor.putString("token", token);
                    editor.putString("name", user.getString("name"));
                    editor.putString("email", user.getString("email"));
                    editor.commit();
                    Intent ii = new Intent(context, MainActivity.class);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(ii);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (message!=null && !message.isEmpty()) {
                Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
            } else if (message.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Server Connection problem .please try again.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Unknown error occured .please try again.", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }
    }
}
