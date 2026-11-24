package com.thaosamyn.wikimapscards.ui.map;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.thaosamyn.wikimapscards.R;
import com.thaosamyn.wikimapscards.wiki.WikiGeoClient;

public class MapFragment extends Fragment {

    // Coordonnées de test en dur (Angers), en attendant la vraie position GPS
    private static final double TEST_LAT = 47.4784;
    private static final double TEST_LON = -0.5632;

    private WikiGeoClient wikiClient;

    public MapFragment() {
        super(R.layout.fragment_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wikiClient = new WikiGeoClient(requireContext());
        wikiClient.fetchNearby(TEST_LAT, TEST_LON);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Cycle de vie : on n'attend plus de réponse si l'écran n'est plus visible
        wikiClient.cancelAll();
    }
}
