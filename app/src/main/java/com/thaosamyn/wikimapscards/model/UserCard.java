package com.thaosamyn.wikimapscards.model;

/** Possession d'une carte par un joueur (table SQLite user_card). */
public class UserCard {

    private final Card card;
    private final int count;
    // pageId du lieu qui a débloqué la carte, null pour un lieu
    private final Long unlockedFrom;
    private final long unlockedAt;

    public UserCard(Card card, int count, Long unlockedFrom, long unlockedAt) {
        this.card = card;
        this.count = count;
        this.unlockedFrom = unlockedFrom;
        this.unlockedAt = unlockedAt;
    }

    public Card getCard() {
        return card;
    }

    public int getCount() {
        return count;
    }

    public Long getUnlockedFrom() {
        return unlockedFrom;
    }

    public long getUnlockedAt() {
        return unlockedAt;
    }

    /** Seuls les doublons peuvent être échangés. */
    public boolean isTradable() {
        return count > 1;
    }
}
