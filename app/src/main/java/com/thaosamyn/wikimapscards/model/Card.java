package com.thaosamyn.wikimapscards.model;

/** Carte = un article Wikipédia. Le pageId sert d'identifiant (table SQLite card). */
public abstract class Card {

    public static final String TYPE_PLACE = "PLACE";
    public static final String TYPE_CONTEXT = "CONTEXT";

    private final long pageId;
    private final String title;
    private final Rarity rarity;
    private final long views30d;
    private final String thumbnail;

    protected Card(long pageId, String title, Rarity rarity, long views30d, String thumbnail) {
        this.pageId = pageId;
        this.title = title;
        this.rarity = rarity;
        this.views30d = views30d;
        this.thumbnail = thumbnail;
    }

    /** Valeur de la colonne type : TYPE_PLACE ou TYPE_CONTEXT. */
    public abstract String getType();

    public long getPageId() {
        return pageId;
    }

    public String getTitle() {
        return title;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public long getViews30d() {
        return views30d;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
