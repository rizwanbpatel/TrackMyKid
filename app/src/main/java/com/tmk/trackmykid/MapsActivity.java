package com.tmk.trackmykid;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.eclipse.paho.android.service.MqttAndroidClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
//        , MqttCallback
{

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private BroadcastReceiver mLocationUpdateBroadcastReceiver;
    private double mLat = 18.5788041;
    private double mLong = 73.7378663;

    //private String clientId = "paho456";
    private MqttAndroidClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_maps);

        final Intent mqttPullerIntentService = new Intent(this, MQTTPullerService.class);
        this.startService(mqttPullerIntentService);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //TODO Fix puller
//        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
//                clientId);


//        try {
//            final IMqttToken mqttConnectToken = client.connect();
//
//            mqttConnectToken.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.d(TAG, " mqttConnectToken onSuccess");
//
//                    client.setCallback(MapsActivity.this);
//
//                    String topic = "Co-ordinates";
//                    try {
//                        final IMqttToken subscribeToken = client.subscribe(topic, 1);
//                        subscribeToken.setActionCallback(new IMqttActionListener() {
//                            @Override
//                            public void onSuccess(IMqttToken asyncActionToken) {
//                            }
//
//                            @Override
//                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                            }
//                        });
//                    } catch (MqttException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.d(TAG, "mqttConnectToken onFailure");
//                    Toast.makeText(getApplicationContext(), "Failure :(", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//            Log.d(TAG, "Message ID : " + String.valueOf(mqttConnectToken.getMessageId()));
//
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }


//        if (mLocationUpdateBroadcastReceiver == null) {
//            mLocationUpdateBroadcastReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    mLat = intent.getExtras().getDouble("lat");
//                    mLong = intent.getExtras().getDouble("long");
//                    Toast.makeText(context, "Lat : " + mLat + "\t Long : " + mLong, Toast.LENGTH_SHORT).show();
//                    final LatLng newLoc = new LatLng(mLat, mLong);
//
//
//                    String mqMessage = intent.getExtras().getString("mq_message");
//                    Log.d(TAG,"lat-long received as via broadcast : ["+mLat+", "+mLong+"]");
//
//                    Log.d(TAG,"mq-message received as via broadcast : "+mqMessage);
//                    Toast.makeText(context,mqMessage,Toast.LENGTH_SHORT).show();
//
//
//                    mMap.addMarker(new MarkerOptions().position(newLoc).title("" + newLoc).snippet("I am here!!"));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLoc, 13));
//                }
//
//            };
//
//            //registerReceiver(mLocationUpdateBroadcastReceiver, new IntentFilter("location_changed"));
//            registerReceiver(mLocationUpdateBroadcastReceiver, new IntentFilter("message_intent"));
//        }


      //  Geofence geofence = new Geofence.Builder();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng eZest = new LatLng(mLat, mLong);
        mMap.addMarker(new MarkerOptions().position(eZest).title("eZest #HackathonPune 2017"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eZest, 12));

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enableButtons();
            } else {
                runtime_permission();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationUpdateBroadcastReceiver != null)
            unregisterReceiver(mLocationUpdateBroadcastReceiver);
    }

    private void enableButtons() {
    }

    private boolean runtime_permission() {

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "I am inside onResume");
        final MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(mLat,mLong));

        if (mLocationUpdateBroadcastReceiver == null) {
            mLocationUpdateBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    double lat = intent.getExtras().getDouble("lat");
                    double longit = intent.getExtras().getDouble("long");
                   // Toast.makeText(context, "Lat : " + lat + "\t Long : " + longit, Toast.LENGTH_SHORT).show();
                 //   intent.getExtras().clear();

                    LatLng newLoc = new LatLng(lat,longit);


                    final Marker marker = mMap.addMarker(markerOptions);
                    marker.setPosition(newLoc);
                    marker.setTitle(""+newLoc);
                    marker.setSnippet("Moved Here");

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLoc, 13));
                }
            };
        }

        //registerReceiver(mLocationUpdateBroadcastReceiver, new IntentFilter("location_changed"));
        registerReceiver(mLocationUpdateBroadcastReceiver, new IntentFilter("message_intent"));

    }

//    @Override
//    public void connectionLost(Throwable cause) {
//
//    }

//    @Override
//    public void messageArrived(String topic, MqttMessage message) throws Exception {
//        Log.d(TAG,"messageArrived : "+topic+ " : "+message.toString());
//        Intent messageIntent = new Intent("message_intent");
//        messageIntent.putExtra("lat",mLat);
//        messageIntent.putExtra("long",mLong);
//        messageIntent.putExtra("mq_message",message.toString());
//        Log.d(TAG,"normal broadcasting....");
//        sendBroadcast(messageIntent);
////        final IMqttToken disconnectToken = client.disconnect();
////        disconnectToken.setActionCallback(new IMqttActionListener() {
////            @Override
////            public void onSuccess(IMqttToken asyncActionToken) {
////                Log.d(TAG,"disconnectToken onSuccess");
////            }
////
////            @Override
////            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
////                Log.d(TAG,"disconnectToken onFailure");
////            }
////        });
//
//    }
//
//    @Override
//    public void deliveryComplete(IMqttDeliveryToken token) {
//
//    }
}
