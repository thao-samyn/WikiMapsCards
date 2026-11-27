package com.thaosamyn.wikimapscards.data;

import static com.thaosamyn.wikimapscards.data.DbHelper.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.thaosamyn.wikimapscards.model.Card;
import com.thaosamyn.wikimapscards.model.ContextCard;
import com.thaosamyn.wikimapscards.model.ContextCategory;
import com.thaosamyn.wikimapscards.model.PlaceCard;
import com.thaosamyn.wikimapscards.model.Rarity;
import com.thaosamyn.wikimapscards.model.UserCard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Accès SQL aux tables card et user_card.
 * Méthodes bloquantes : à n'appeler que depuis un thread de fond (voir CardRepository).
 */
public class CardDao {

    // Jointure commune à toutes les lectures de cartes possédées
    private static final String SELECT_USER_CARDS = "SELECT c.*, u." + COL_COUNT
            + ", u." + COL_UNLOCKED_FROM + ", u." + COL_UNLOCKED_AT
            + " FROM " + TABLE_USER_CARD + " u JOIN " + TABLE_CARD + " c"
            + " ON c." + COL_PAGE_ID + " = u." + COL_PAGE_ID
            + " WHERE u." + COL_USER_UID + " = ?";

    private final DbHelper helper;

    public CardDao(DbHelper helper) {
        this.helper = helper;
    }

    /**
     * Ajoute la carte au joueur : nouvelle ligne si elle n'est pas possédée,
     * sinon count + 1 (doublon échangeable).
     */
    public void insertOrIncrement(Card card, String userUid, Long unlockedFrom) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            // La carte elle-même n'est enregistrée qu'une fois (rareté figée au déblocage)
            db.insertWithOnConflict(TABLE_CARD, null, toValues(card), SQLiteDatabase.CONFLICT_IGNORE);

            if (owns(userUid, card.getPageId())) {
                db.execSQL("UPDATE " + TABLE_USER_CARD + " SET " + COL_COUNT + " = " + COL_COUNT + " + 1"
                                + " WHERE " + COL_USER_UID + " = ? AND " + COL_PAGE_ID + " = ?",
                        new Object[]{userUid, card.getPageId()});
            } else {
                ContentValues values = new ContentValues();
                values.put(COL_USER_UID, userUid);
                values.put(COL_PAGE_ID, card.getPageId());
                values.put(COL_COUNT, 1);
                values.put(COL_UNLOCKED_FROM, unlockedFrom);
                values.put(COL_UNLOCKED_AT, System.currentTimeMillis());
                db.insert(TABLE_USER_CARD, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public boolean owns(String userUid, long pageId) {
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT 1 FROM " + TABLE_USER_CARD
                        + " WHERE " + COL_USER_UID + " = ? AND " + COL_PAGE_ID + " = ?",
                new String[]{userUid, String.valueOf(pageId)})) {
            return cursor.moveToFirst();
        }
    }

    /** pageId des lieux déjà capturés (pour colorer les marqueurs). */
    public Set<Long> getOwnedPlaceIds(String userUid) {
        Set<Long> ids = new HashSet<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT u." + COL_PAGE_ID + " FROM " + TABLE_USER_CARD + " u JOIN " + TABLE_CARD + " c"
                        + " ON c." + COL_PAGE_ID + " = u." + COL_PAGE_ID
                        + " WHERE u." + COL_USER_UID + " = ? AND c." + COL_TYPE + " = ?",
                new String[]{userUid, Card.TYPE_PLACE})) {
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(0));
            }
        }
        return ids;
    }

    /** Lieux capturés, du plus récent au plus ancien (écran collection). */
    public List<UserCard> getPlaces(String userUid) {
        return queryUserCards(SELECT_USER_CARDS + " AND c." + COL_TYPE + " = ?"
                        + " ORDER BY u." + COL_UNLOCKED_AT + " DESC",
                userUid, Card.TYPE_PLACE);
    }

    /** Cartes de contexte débloquées par un lieu (détail d'un lieu). */
    public List<UserCard> getCardsUnlockedFrom(String userUid, long placeId) {
        return queryUserCards(SELECT_USER_CARDS + " AND u." + COL_UNLOCKED_FROM + " = ?"
                        + " ORDER BY c." + COL_TITLE,
                userUid, String.valueOf(placeId));
    }

    private List<UserCard> queryUserCards(String sql, String... args) {
        List<UserCard> result = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                int fromIndex = cursor.getColumnIndexOrThrow(COL_UNLOCKED_FROM);
                Long unlockedFrom = cursor.isNull(fromIndex) ? null : cursor.getLong(fromIndex);
                result.add(new UserCard(
                        cardFromCursor(cursor),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COUNT)),
                        unlockedFrom,
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_UNLOCKED_AT))));
            }
        }
        return result;
    }

    /** Reconstruit une PlaceCard ou une ContextCard selon la colonne type. */
    private static Card cardFromCursor(Cursor cursor) {
        long pageId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PAGE_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
        Rarity rarity = Rarity.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_RARITY)));
        long views = cursor.getLong(cursor.getColumnIndexOrThrow(COL_VIEWS));
        String thumbnail = cursor.getString(cursor.getColumnIndexOrThrow(COL_THUMBNAIL));

        if (Card.TYPE_PLACE.equals(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)))) {
            return new PlaceCard(pageId, title, rarity, views, thumbnail,
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LON)));
        }
        ContextCategory category = ContextCategory.valueOf(
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        return new ContextCard(pageId, title, rarity, views, thumbnail, category);
    }

    private static ContentValues toValues(Card card) {
        ContentValues values = new ContentValues();
        values.put(COL_PAGE_ID, card.getPageId());
        values.put(COL_TITLE, card.getTitle());
        values.put(COL_TYPE, card.getType());
        values.put(COL_RARITY, card.getRarity().name());
        values.put(COL_VIEWS, card.getViews30d());
        values.put(COL_THUMBNAIL, card.getThumbnail());

        if (card instanceof PlaceCard) {
            PlaceCard place = (PlaceCard) card;
            values.put(COL_LAT, place.getLat());
            values.put(COL_LON, place.getLon());
        } else if (card instanceof ContextCard) {
            values.put(COL_CATEGORY, ((ContextCard) card).getCategory().name());
        }
        return values;
    }
}
