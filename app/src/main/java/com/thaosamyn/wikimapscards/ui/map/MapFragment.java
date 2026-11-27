package com.thaosamyn.wikimapscards.ui.map;

import android.Manifest;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thaosamyn.wikimapscards.R;
import com.thaosamyn.wikimapscards.geo.LocationHelper;
import com.thaosamyn.wikimapscards.model.WikiPlace;
import com.thaosamyn.wikimapscards.wiki.WikiGeoClient;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, WikiGeoClient.Listener{

    private static final String TAG = "MapFragment";

    // On ne réinterroge Wikipédia que si le joueur s'est éloigné de plus de 200 m
    private static final float REFRESH_DISTANCE_M = 200f;
    private static final float CAPTURE_DISTANCE = 50f;

    private WikiGeoClient wikiClient;
    private LocationHelper locationHelper;
    private Location lastSearchLocation;
    // Dernière position reçue : affichée dès que la carte est prête si le GPS a répondu avant
    private Location lastLocation;
    private GoogleMap map;
    private Marker userMarker;
    private Circle userCircle;


    // Doit être enregistré avant que le fragment soit démarré, d'où le champ
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    this::onPermissionResult);

    public MapFragment() {
        super(R.layout.fragment_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);


        // initialisation des helpers
        wikiClient = new WikiGeoClient(requireContext(), this);
        locationHelper = new LocationHelper(requireContext(), this::onLocation);


        if (!LocationHelper.hasPermission(requireContext())) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        locationHelper.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
        wikiClient.cancelAll();
        lastSearchLocation = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        map = null;
        userMarker = null;
        userCircle = null;
    }

    private void onPermissionResult(Map<String, Boolean> result) {
        if (LocationHelper.hasPermission(requireContext())) {
            locationHelper.start();
        } else {
            Toast.makeText(requireContext(), R.string.location_permission_denied, Toast.LENGTH_LONG).show();
        }
    }

    private void onLocation(@NonNull Location location) {
        Log.d(TAG, "Position reçue : " + location.getLatitude() + ", " + location.getLongitude());
        lastLocation = location;

        if (lastSearchLocation == null || location.distanceTo(lastSearchLocation) > REFRESH_DISTANCE_M) {
            lastSearchLocation = location;
            wikiClient.fetchNearby(location.getLatitude(), location.getLongitude());
        }

        showUserPosition(location);
        showPlaces(location);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Log.d(TAG, "Carte prête");
        map = googleMap;

        // initialisation du marqueur et la zone utilisateur
        userMarker = null;
        userCircle = null;

        // configuration de l'affichage du zoom et de la direction

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);

        // Le GPS a pu répondre avant la carte : on affiche la position déjà connue
        if (lastLocation != null) {
            showUserPosition(lastLocation);
        }
    }

    public void showUserPosition(Location location) {

        // GPS avant la map peut-être
        if (map == null) {
            return;
        }

        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

        if (userMarker == null) {

            userMarker = map.addMarker(new MarkerOptions().position(position));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 20));

            CircleOptions circleOptions = new CircleOptions()
                    .center(position)
                    .radius(CAPTURE_DISTANCE)
                    .strokeColor(Color.argb(255, 25, 118, 210))
                    .strokeWidth(4f)
                    .fillColor(Color.argb(60, 25, 118, 210));

            userCircle = map.addCircle(circleOptions);
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 20));
            userMarker.setPosition(position);
            userCircle.setCenter(position);
        }
    }

    public void showPlaces(Location location){

    }

    @Override
    public void onPlaces(List<WikiPlace> places) {
        // Temporaire : les marqueurs des lieux viendront ensuite
        Log.d(TAG, places.size() + " lieux trouvés");
    }

    @Override
    public void onError(String message) {
        Log.e(TAG, "Recherche Wikipédia impossible : " + message);
        Toast.makeText(requireContext(), R.string.wiki_error, Toast.LENGTH_SHORT).show();
    }
}
