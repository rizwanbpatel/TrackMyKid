package com.tmk.trackmykid;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rizwan on 9/23/17.
 */

public class MQTTPullerService extends IntentService implements MqttCallback {

    private static final String TAG = "MQTTPullerService";
    private static final long SYNCH_TIME = 30 * 1000;
    private static final String clientId = "paho123";
    private final String TOPIC = "mykid/location";
    private Set<String> visitedLatLng = new HashSet<>();
    private MqttAndroidClient mqttPullerClient;


    private Handler mHandler;
    private Runnable runnableMQTTPuller = new Runnable() {
        @Override
        public void run() {
            synchLocationData();
         //   mHandler.postDelayed(runnableMQTTPuller, SYNCH_TIME);
        }
    };

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MQTTPullerService(String name) {
        super(name);
    }

    public MQTTPullerService() {
        this("mypuller");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        mHandler = new Handler();

        mHandler.post(runnableMQTTPuller);
        return START_STICKY;
    }

    private synchronized void synchLocationData() {
        try {
            mqttPullerClient = new MqttAndroidClient(this.getApplicationContext(), Constants.MQTT_SERVER, clientId);
            final IMqttToken mqttToken = mqttPullerClient.connect();
            mqttToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, " synch mqttToken onSuccess");
                    mqttPullerClient.setCallback(MQTTPullerService.this);
                    try {
                        final IMqttToken subscribeToken = mqttPullerClient.subscribe(TOPIC, 1);

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, " mqttToken onFailure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String dataString = intent.getDataString();
        Log.d(TAG, "onHandleIntent : " + dataString);
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (message != null && message.toString().length() > 0) {

            final boolean toBeVisited = visitedLatLng.add(message.toString());
            if (toBeVisited) {
                Log.d(TAG, "synch puller messageArrived : " + message.toString());

                Intent pullerIntent = new Intent("message_intent");

                final String[] splitMsg = message.toString().split(",");
                if (splitMsg != null && splitMsg.length == 2) {
                    double lat = Double.parseDouble(splitMsg[0]);
                    double longitude = Double.parseDouble(splitMsg[1]);
                    pullerIntent.putExtra("lat", lat);
                    pullerIntent.putExtra("long", longitude);
                } else {
                    Log.d(TAG, "Going to hell..............");
                }

                pullerIntent.putExtra("mq_message", message.toString());
                Log.d(TAG, "synch broadcasting....");
                sendBroadcast(pullerIntent);
            }

        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            Log.d(TAG, "deliveryComplete");
            token.getMessage().clearPayload();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mqttPullerClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
