package com.thaosamyn.wikimapscards.model;

/** Rareté d'une carte, calculée une seule fois à partir des vues Wikipédia sur 30 jours. */
public enum Rarity {
    COMMON,
    UNCOMMON,
    RARE,
    LEGENDARY;

    public static Rarity fromViews(long views30d) {
        if (views30d >= 50_000) {
            return LEGENDARY;
        } else if (views30d >= 5_000) {
            return RARE;
        } else if (views30d >= 500) {
            return UNCOMMON;
        }
        return COMMON;
    }
}
