package com.tmk.trackmykid;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class LocationProviderService extends Service {
    private static String TAG = "LocationProviderService";
    private final String TOPIC = "mykid/location";
    byte[] encodedPayload;
    private String clientId;
    private MqttAndroidClient client;
    private IMqttToken mqttToken;
    private String payload;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private String ENCODE_CHARSET;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        connectWithMQTTService();

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent locationUpdateIntent = new Intent("location_changed");
                Log.d(TAG, "onLocationChanged");
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();
                locationUpdateIntent.putExtra("lat", latitude);
                locationUpdateIntent.putExtra("long", longitude);
                //sendBroadcast(locationUpdateIntent);

                payload = latitude + ", " + longitude;

                publishPayloadToMQTTServer(payload);

                notifyCoordinate("coord : [" + latitude + ",  " + longitude + "]");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        };
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 50, mLocationListener);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    private void notifyCoordinate(String coordinates) {

//        Intent geoFenceActivityIntent = new Intent(this, GeoFenceActivity.class);
//        geoFenceActivityIntent.putExtra("coordinates",coordinates);
//        geoFenceActivityIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
//        startActivity(geoFenceActivityIntent);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Location")
                .setContentText(coordinates);
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, notificationBuilder.build());
    }


    private void connectWithMQTTService() {
        clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), Constants.MQTT_SERVER, clientId);
        encodedPayload = new byte[0];

        try {
            mqttToken = client.connect();
            mqttToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishPayloadToMQTTServer(final String payload) {

        ENCODE_CHARSET = "UTF-8";
        try {
            encodedPayload = payload.getBytes(ENCODE_CHARSET);
            MqttMessage mqttMessage = new MqttMessage(encodedPayload);
            mqttMessage.setRetained(true);
            client.publish(TOPIC, mqttMessage);

        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }
}
