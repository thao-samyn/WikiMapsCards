package com.thaosamyn.wikimapscards.geo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {

    /** Reçoit chaque nouvelle position, sur le thread principal. */
    public interface Listener {
        void onLocation(@NonNull Location location);
    }

    private static final String TAG = "LocationHelper";

    // Toutes les 10 secondes et si on a plus de 10 mètres de différence avec la position d'avant
    private static final long INTERVAL_MS = 10000;
    private static final float MIN_DISTANCE_M = 10f;

    private final Context context;
    private final FusedLocationProviderClient client;
    private final LocationCallback callback;

    public LocationHelper(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        client = LocationServices.getFusedLocationProviderClient(this.context);
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    listener.onLocation(location);
                }
            }
        };
    }

    /** Position précise autorisée ? (nécessaire pour la capture à 50 m) */
    public static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** À appeler dans onResume. Ne fais rien si la permission manque. */
    public void start() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permission de position exacte manquante : suivi GPS non démarré");
            return;
        }
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS)
                .setMinUpdateDistanceMeters(MIN_DISTANCE_M)
                .build();
        client.requestLocationUpdates(request, callback, Looper.getMainLooper());
        Log.d(TAG, "Suivi GPS démarré");
    }

    /** À appeler dans onPause : le GPS peut s'éteindre. */
    public void stop() {
        client.removeLocationUpdates(callback);
    }
}
