package com.thaosamyn.wikimapscards.wiki;

import android.util.Log;

import com.thaosamyn.wikimapscards.model.WikiPlace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class WikiGeoParser {

    private static final String TAG = "WikiGeoParser";

    private WikiGeoParser() {
        // Classe utilitaire : que des méthodes static, pas d'instance
    }

    public static List<WikiPlace> parse(JSONObject response) {
        List<WikiPlace> places = new ArrayList<>();

        JSONObject query = response.optJSONObject("query");
        if (query == null) {
            return places;
        }

        try {
            JSONArray pages = query.getJSONArray("pages");
            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i);

                // Une page sans coordonnées ne peut pas être placée sur la carte
                JSONArray coordinates = page.optJSONArray("coordinates");
                if (coordinates == null || coordinates.length() == 0) {
                    continue;
                }
                JSONObject coord = coordinates.getJSONObject(0);

                // Description et miniature sont facultatives
                String description = page.optString("description", null);
                String thumbnailUrl = null;
                JSONObject thumbnail = page.optJSONObject("thumbnail");
                if (thumbnail != null) {
                    thumbnailUrl = thumbnail.optString("source", null);
                }

                places.add(new WikiPlace(
                        page.getInt("pageid"),
                        page.getString("title"),
                        description,
                        coord.getDouble("lat"),
                        coord.getDouble("lon"),
                        thumbnailUrl));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Réponse geosearch invalide", e);
        }
        return places;
    }
}
