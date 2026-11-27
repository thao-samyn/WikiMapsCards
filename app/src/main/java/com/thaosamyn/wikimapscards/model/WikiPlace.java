package com.thaosamyn.wikimapscards.model;

public final class WikiPlace {

    private final int pageId;
    private final String title;
    private final String description;
    private final double lat;
    private final double lon;
    private final String thumbnailUrl;

    public WikiPlace(int pageId, String title, String description, double lat, double lon, String thumbnailUrl) {
        this.pageId = pageId;
        this.title = title;
        this.description = description;
        this.lat = lat;
        this.lon = lon;
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getPageId() {
        return pageId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

}
