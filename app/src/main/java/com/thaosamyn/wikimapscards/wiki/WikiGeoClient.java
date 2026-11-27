package com.thaosamyn.wikimapscards.wiki;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.thaosamyn.wikimapscards.model.WikiPlace;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WikiGeoClient {

    private static final String TAG = "WikiGeoClient";
    private static final String API_URL = "https://fr.wikipedia.org/w/api.php";
    private static final String USER_AGENT = "WikiMapsCards/1.0 (projet étudiant Android)";
    private static final int RADIUS_M = 2000;
    private static final int LIMIT = 50;
    private final RequestQueue queue;
    private final Listener listener;

    public interface Listener {
        void onPlaces(List<WikiPlace> places);
        void onError(String message);
    }

    public WikiGeoClient(Context context, Listener listener) {
        this.listener = listener;
        // Contexte Application : évite de garder une référence vers l'Activity (fuite mémoire)
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void fetchNearby(double lat, double lon) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, buildNearbyUrl(lat, lon), null,
                response -> listener.onPlaces(WikiGeoParser.parse(response)),
                error -> {
                    Log.e(TAG, "Erreur geosearch : " + error);
                    listener.onError(error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() {
                return Collections.singletonMap("User-Agent", USER_AGENT);
            }
        };
        request.setTag(TAG);
        queue.add(request);
    }

    /** Télécharge la miniature d'un lieu ; en cas d'erreur on garde simplement l'image par défaut. */
    public void fetchThumbnail(String url, Response.Listener<Bitmap> onBitmap) {
        ImageRequest request = new ImageRequest(url, onBitmap, 0, 0, null, null,
                error -> Log.e(TAG, "Erreur miniature : " + error)) {
            @Override
            public Map<String, String> getHeaders() {
                // upload.wikimedia.org exige aussi un User-Agent identifiable
                return Collections.singletonMap("User-Agent", USER_AGENT);
            }
        };
        request.setTag(TAG);
        queue.add(request);
    }

    /** Annule les requêtes en cours (à appeler quand l'écran n'est plus visible). */
    public void cancelAll() {
        queue.cancelAll(TAG);
    }

    private String buildNearbyUrl(double lat, double lon) {
        return Uri.parse(API_URL).buildUpon()
                .appendQueryParameter("action", "query")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("formatversion", "2")
                .appendQueryParameter("generator", "geosearch")
                .appendQueryParameter("ggscoord", lat + "|" + lon)
                .appendQueryParameter("ggsradius", String.valueOf(RADIUS_M))
                .appendQueryParameter("ggslimit", String.valueOf(LIMIT))
                .appendQueryParameter("prop", "coordinates|pageimages|description")
                .appendQueryParameter("colimit", "max")
                .appendQueryParameter("piprop", "thumbnail")
                .appendQueryParameter("pithumbsize", "200")
                .appendQueryParameter("pilimit", String.valueOf(LIMIT))
                .build()
                .toString();
    }
}
