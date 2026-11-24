package com.thaosamyn.wikimapscards.ui.map;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.thaosamyn.wikimapscards.R;
import com.thaosamyn.wikimapscards.geo.LocationHelper;
import com.thaosamyn.wikimapscards.wiki.WikiGeoClient;

import java.util.Map;

public class MapFragment extends Fragment {

    // On ne réinterroge Wikipédia que si le joueur s'est éloigné de plus de 200 m
    private static final float REFRESH_DISTANCE_M = 200f;

    private WikiGeoClient wikiClient;
    private LocationHelper locationHelper;
    private Location lastSearchLocation;

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
        wikiClient = new WikiGeoClient(requireContext());
        locationHelper = new LocationHelper(requireContext(), this::onLocation);

        if (!LocationHelper.hasPermission(requireContext())) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
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
        // Cycle de vie : on n'attend plus de réponse si l'écran n'est plus visible,
        // et on relancera la recherche au prochain fix
        wikiClient.cancelAll();
        lastSearchLocation = null;
    }

    private void onPermissionResult(Map<String, Boolean> result) {
        if (LocationHelper.hasPermission(requireContext())) {
            locationHelper.start();
        } else {
            Toast.makeText(requireContext(), R.string.location_permission_denied, Toast.LENGTH_LONG).show();
        }
    }

    private void onLocation(@NonNull Location location) {
        if (lastSearchLocation == null || location.distanceTo(lastSearchLocation) > REFRESH_DISTANCE_M) {
            lastSearchLocation = location;
            wikiClient.fetchNearby(location.getLatitude(), location.getLongitude());
        }
        // À venir : recalculer la distance aux lieux connus pour savoir lesquels sont capturables
    }
}
