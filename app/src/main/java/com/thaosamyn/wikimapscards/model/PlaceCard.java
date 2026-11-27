package com.thaosamyn.wikimapscards.model;

/** Carte d'un lieu géolocalisé, obtenue par capture. */
public class PlaceCard extends Card {

    private final double lat;
    private final double lon;

    public PlaceCard(long pageId, String title, Rarity rarity, long views30d, String thumbnail,
                     double lat, double lon) {
        super(pageId, title, rarity, views30d, thumbnail);
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String getType() {
        return TYPE_PLACE;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
