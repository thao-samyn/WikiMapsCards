package com.thaosamyn.wikimapscards.model;

/** Carte de contexte (personnage, événement, œuvre) débloquée par la capture d'un lieu. */
public class ContextCard extends Card {

    private final ContextCategory category;

    public ContextCard(long pageId, String title, Rarity rarity, long views30d, String thumbnail,
                       ContextCategory category) {
        super(pageId, title, rarity, views30d, thumbnail);
        this.category = category;
    }

    @Override
    public String getType() {
        return TYPE_CONTEXT;
    }

    public ContextCategory getCategory() {
        return category;
    }
}
