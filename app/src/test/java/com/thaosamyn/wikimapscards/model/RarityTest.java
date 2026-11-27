package com.thaosamyn.wikimapscards.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RarityTest {

    @Test
    public void fromViews_respecteLesSeuils() {
        assertEquals(Rarity.COMMON, Rarity.fromViews(0));
        assertEquals(Rarity.COMMON, Rarity.fromViews(499));
        assertEquals(Rarity.UNCOMMON, Rarity.fromViews(500));
        assertEquals(Rarity.UNCOMMON, Rarity.fromViews(4_999));
        assertEquals(Rarity.RARE, Rarity.fromViews(5_000));
        assertEquals(Rarity.RARE, Rarity.fromViews(49_999));
        assertEquals(Rarity.LEGENDARY, Rarity.fromViews(50_000));
    }
}
