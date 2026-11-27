package com.thaosamyn.wikimapscards.ui.map;

import android.Manifest;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.util.ArrayList;
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
    // Marqueurs des lieux Wikipédia affichés, pour pouvoir les retirer au prochain résultat
    private final List<Marker> placeMarkers = new ArrayList<>();
    // Derniers lieux reçus : affichés dès que la carte est prête si Wikipédia a répondu avant
    private List<WikiPlace> lastPlaces;

    // Panneau de détail du lieu cliqué
    private View placePanel;
    private ImageView placeImage;
    private TextView placeTitle;
    private TextView placeDescription;
    // Lieu affiché dans le panneau : évite d'afficher une image arrivée en retard pour un autre lieu
    private WikiPlace selectedPlace;


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

        placePanel = view.findViewById(R.id.place_panel);
        placeImage = view.findViewById(R.id.place_image);
        placeTitle = view.findViewById(R.id.place_title);
        placeDescription = view.findViewById(R.id.place_description);

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
        placeMarkers.clear();
        placePanel = null;
        placeImage = null;
        placeTitle = null;
        placeDescription = null;
        selectedPlace = null;
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
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Log.d(TAG, "Carte prête");
        map = googleMap;

        // initialisation du marqueur et la zone utilisateur
        userMarker = null;
        userCircle = null;
        placeMarkers.clear();

        // configuration de l'affichage du zoom et de la direction

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);

        // Clic sur un lieu : panneau de détail ; clic ailleurs : on le ferme
        map.setOnMarkerClickListener(this::onMarkerClick);
        map.setOnMapClickListener(latLng -> hidePlace());

        // Le GPS a pu répondre avant la carte : on affiche la position déjà connue
        if (lastLocation != null) {
            showUserPosition(lastLocation);
        }
        // Idem pour les lieux Wikipédia
        if (lastPlaces != null) {
            showPlaces(lastPlaces);
        }
    }

    public void showUserPosition(Location location) {

        // GPS avant la map peut-être
        if (map == null) {
            return;
        }

        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

        if (userMarker == null) {

            userMarker = map.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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

    private void showPlaces(List<WikiPlace> places) {
        if (map == null) {
            return;
        }

        // On remplace les anciens marqueurs par ceux de la nouvelle recherche
        for (Marker marker : placeMarkers) {
            marker.remove();
        }
        placeMarkers.clear();

        for (WikiPlace place : places) {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(place.getLat(), place.getLon()))
                    .title(place.getTitle())
                    .snippet(place.getDescription())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            if (marker != null) {
                // Permet de retrouver le lieu au clic (capture)
                marker.setTag(place);
                placeMarkers.add(marker);
            }
        }
    }

    private boolean onMarkerClick(Marker marker) {
        // Le marqueur du joueur n'a pas de tag
        if (marker.getTag() instanceof WikiPlace) {
            showPlace((WikiPlace) marker.getTag());
        }
        // false : on garde le comportement par défaut (centrage + info-bulle)
        return false;
    }

    private void showPlace(WikiPlace place) {
        selectedPlace = place;
        placeTitle.setText(place.getTitle());
        placeDescription.setText(place.getDescription());
        placeImage.setImageResource(R.drawable.ic_map);
        placePanel.setVisibility(View.VISIBLE);

        if (place.getThumbnailUrl() != null) {
            wikiClient.fetchThumbnail(place.getThumbnailUrl(), bitmap -> {
                // L'utilisateur a pu cliquer sur un autre lieu entre-temps
                if (selectedPlace == place) {
                    placeImage.setImageBitmap(bitmap);
                }
            });
        }
    }

    private void hidePlace() {
        selectedPlace = null;
        placePanel.setVisibility(View.GONE);
    }

    @Override
    public void onPlaces(List<WikiPlace> places) {
        Log.d(TAG, places.size() + " lieux trouvés");
        lastPlaces = places;
        showPlaces(places);
    }

    @Override
    public void onError(String message) {
        Log.e(TAG, "Recherche Wikipédia impossible : " + message);
        Toast.makeText(requireContext(), R.string.wiki_error, Toast.LENGTH_SHORT).show();
    }
}
