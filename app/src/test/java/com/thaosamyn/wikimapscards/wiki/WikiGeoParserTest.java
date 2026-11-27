package com.thaosamyn.wikimapscards.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.thaosamyn.wikimapscards.model.WikiPlace;

import org.json.JSONObject;
import org.junit.Test;

import java.util.List;

public class WikiGeoParserTest {

    private static final double DELTA = 1e-6;

    /** Construit une réponse geosearch contenant les pages données (JSON). */
    private static JSONObject response(String pages) throws Exception {
        return new JSONObject("{\"batchcomplete\":true,\"query\":{\"pages\":[" + pages + "]}}");
    }

    @Test
    public void reponseSansQuery_listeVide() throws Exception {
        List<WikiPlace> places = WikiGeoParser.parse(new JSONObject("{\"batchcomplete\":true}"));
        assertTrue(places.isEmpty());
    }

    @Test
    public void pageComplete_tousLesChampsLus() throws Exception {
        List<WikiPlace> places = WikiGeoParser.parse(response(
                "{\"pageid\":123,\"title\":\"Château d'Angers\","
                        + "\"description\":\"château fort\","
                        + "\"coordinates\":[{\"lat\":47.47,\"lon\":-0.56}],"
                        + "\"thumbnail\":{\"source\":\"https://img/chateau.jpg\"}}"));

        assertEquals(1, places.size());
        WikiPlace place = places.get(0);
        assertEquals(123, place.getPageId());
        assertEquals("Château d'Angers", place.getTitle());
        assertEquals("château fort", place.getDescription());
        assertEquals(47.47, place.getLat(), DELTA);
        assertEquals(-0.56, place.getLon(), DELTA);
        assertEquals("https://img/chateau.jpg", place.getThumbnailUrl());
    }

    @Test
    public void pageSansCoordonnees_ignoree() throws Exception {
        List<WikiPlace> places = WikiGeoParser.parse(response(
                "{\"pageid\":1,\"title\":\"Sans coordonnées\"},"
                        + "{\"pageid\":2,\"title\":\"Avec\",\"coordinates\":[{\"lat\":1,\"lon\":2}]}"));

        assertEquals(1, places.size());
        assertEquals(2, places.get(0).getPageId());
    }

    @Test
    public void champsFacultatifsAbsents_null() throws Exception {
        List<WikiPlace> places = WikiGeoParser.parse(response(
                "{\"pageid\":1,\"title\":\"Minimal\",\"coordinates\":[{\"lat\":1,\"lon\":2}]}"));

        assertNull(places.get(0).getDescription());
        assertNull(places.get(0).getThumbnailUrl());
    }
}
