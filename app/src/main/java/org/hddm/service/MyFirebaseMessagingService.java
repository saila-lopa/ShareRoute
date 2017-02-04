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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.hddm.shareroute.MainActivity;
import org.hddm.shareroute.R;
import org.hddm.shareroute.RouteViewActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFMService";
    private NotificationManager mNM;
    private static final  int NOTIFICATION_ID = 100;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d(TAG, "FCM Notification Message: " +
                remoteMessage.getNotification());
        Log.d(TAG, "FCM Data Message: " + remoteMessage.getData());
        showNotification(remoteMessage);
    }
    private void showNotification(RemoteMessage remoteMessage) {
        try{
            if(remoteMessage!=null) {
                Map<String, String> data = remoteMessage.getData();

                String message = data.get("message");
                CharSequence text = message;

                String extras = data.get("extras");
                Intent intent;
                JSONObject extrasJSON;
                int soundFileId = R.raw.perseus;
                try {
                    extrasJSON = new JSONObject(extras);
                    if(extrasJSON.has("routeId")) {
                        int routeId = extrasJSON.getInt("routeId");
                        intent = new Intent(this, RouteViewActivity.class);
                        intent.putExtra("routeId", routeId);
                    }  else {
                        intent = new Intent(this, MainActivity.class);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    intent = new Intent(this, MainActivity.class);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent contentIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                // Set the info for the views that show in the notification panel.
                Notification notification = new Notification.Builder(this)
                        .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL)
                        .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                        .setTicker(text)  // the status text
                        .setWhen(System.currentTimeMillis())  // the time stamp
                        .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                        .setContentText(text)  // the contents of the entry
                        .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                        .build();
//        notification.sound = soundUri;
//
                notification.flags = Notification.FLAG_AUTO_CANCEL; //| Notification.DEFAULT_SOUND ;Notification.DEFAULT_LIGHTS |
                // Send the notification.
                mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + soundFileId);
                mNM.notify(NOTIFICATION_ID, notification);
            }
        }catch (Exception ex) {
            Log.d("FirebaseException", ex.getMessage());
        }
    }
}
