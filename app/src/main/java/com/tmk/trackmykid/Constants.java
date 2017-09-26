package com.tmk.trackmykid;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by rizwan on 9/24/17.
 */

public class Constants {

    public static final String MQTT_SERVER = "tcp://broker.hivemq.com:1883";
    public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km
    /**
     * Map for storing information about airports in the San Francisco bay area.
     */
    public static final HashMap<String, LatLng> DBO_AREA_LANDMARKS = new HashMap<>();
    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";
    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";
    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    /**
     * For this sample, geofences expire after twelve hours.
     */
    static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    static {
        // San Francisco International Airport.
        DBO_AREA_LANDMARKS.put("DBO", new LatLng(18.552804, 73.893839));

        // Googleplex.
        DBO_AREA_LANDMARKS.put("DBO_COMPOUND", new LatLng(18.552763, 73.894087));
    }
}
