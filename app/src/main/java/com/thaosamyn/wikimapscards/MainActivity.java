package com.thaosamyn.wikimapscards;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.thaosamyn.wikimapscards.ui.collection.CollectionFragment;
import com.thaosamyn.wikimapscards.ui.map.MapFragment;
import com.thaosamyn.wikimapscards.ui.trade.TradesFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialisation du premier fragment
        if(savedInstanceState == null) {
            showFragment(new MapFragment());
        }

        // Installation du Listener pour le BottomNav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                showFragment(new MapFragment());
            } else if (id == R.id.nav_collection) {
                showFragment(new CollectionFragment());
            } else if (id == R.id.nav_trades) {
                showFragment(new TradesFragment());
            } else {
                return false;
            }
            return true;
        });

        // Re-clic sur l'onglet courant : on ne recrée pas le fragment
        bottomNav.setOnItemReselectedListener(item -> { });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}