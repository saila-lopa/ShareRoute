package org.hddm.shareroute;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * Created by root on 9/7/16.
 */
public class SplashScreenActivity extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash_screen);

        ActionBar bar = getActionBar();
        if(bar!=null)
            bar.hide();
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashScreenActivity.this,LoginActivity.class);
                SplashScreenActivity.this.startActivity(mainIntent);
                SplashScreenActivity.this.finish();

                SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
                String userId = sharedPref.getString("userId", "");

                if(userId==null || userId.isEmpty())
                {
                    Intent ii = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(ii);
                }
                else
                {
                    Intent ii = new Intent(SplashScreenActivity.this, MainActivity.class);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(ii);
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}