/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hddm.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import org.hddm.shareroute.R;
import org.hddm.utils.BaseUrl;
import org.hddm.utils.HttpJsonPost;
import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    /**
     * The Application's current Instance ID token is no longer valid
     * and thus a new one must be requested.
     */
    @Override
    public void onTokenRefresh() {
        // If you need to handle the generation of a token, initially or
        // after a refresh this is where you should do that.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FCM Token: " + token);
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
        String userId = sharedPref.getString("userId", "");
        if(!userId.isEmpty() && userId.length()>0){
            sendUserFcmToken(userId, token);
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("fcmToken", token);
            editor.commit();
        }
    }
    private void sendUserFcmToken(String userId, String fcmToken) {
        JSONObject json = new JSONObject();
        try {
            json.put("userId", "" + userId);
            json.put("fcmToken", fcmToken);
            String url = BaseUrl.HTTP + "save-fcm-token";
            String results = HttpJsonPost.POST(url, json);
            JSONObject jsonOb = new JSONObject(results);
            if (jsonOb.has("userId") && jsonOb.getString("userId") != null) {
                SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("notificationToken",jsonOb.getString("fcmToken"));
                editor.commit();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
