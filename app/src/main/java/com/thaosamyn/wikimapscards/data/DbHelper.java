package com.thaosamyn.wikimapscards.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Création de la base locale (copie hors ligne des cartes, Firestore fait foi). */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "wikimapscards.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_CARD = "card";
    public static final String TABLE_USER_CARD = "user_card";

    // Colonnes communes aux deux tables
    public static final String COL_PAGE_ID = "page_id";

    // Table card
    public static final String COL_TITLE = "title";
    public static final String COL_TYPE = "type";
    public static final String COL_CATEGORY = "category";
    public static final String COL_LAT = "lat";
    public static final String COL_LON = "lon";
    public static final String COL_RARITY = "rarity";
    public static final String COL_VIEWS = "views_30d";
    public static final String COL_THUMBNAIL = "thumbnail";

    // Table user_card
    public static final String COL_USER_UID = "user_uid";
    public static final String COL_COUNT = "count";
    public static final String COL_UNLOCKED_FROM = "unlocked_from";
    public static final String COL_UNLOCKED_AT = "unlocked_at";

    private static final String CREATE_CARD = "CREATE TABLE " + TABLE_CARD + " ("
            + COL_PAGE_ID + " INTEGER PRIMARY KEY, "
            + COL_TITLE + " TEXT NOT NULL, "
            + COL_TYPE + " TEXT NOT NULL, "
            + COL_CATEGORY + " TEXT, "
            + COL_LAT + " REAL, "
            + COL_LON + " REAL, "
            + COL_RARITY + " TEXT NOT NULL, "
            + COL_VIEWS + " INTEGER, "
            + COL_THUMBNAIL + " TEXT)";

    private static final String CREATE_USER_CARD = "CREATE TABLE " + TABLE_USER_CARD + " ("
            + COL_USER_UID + " TEXT NOT NULL, "
            + COL_PAGE_ID + " INTEGER NOT NULL REFERENCES " + TABLE_CARD + "(" + COL_PAGE_ID + "), "
            + COL_COUNT + " INTEGER NOT NULL DEFAULT 1, "
            + COL_UNLOCKED_FROM + " INTEGER REFERENCES " + TABLE_CARD + "(" + COL_PAGE_ID + "), "
            + COL_UNLOCKED_AT + " INTEGER NOT NULL, "
            + "PRIMARY KEY (" + COL_USER_UID + ", " + COL_PAGE_ID + "))";

    public DbHelper(Context context) {
        // Contexte Application : la base vit aussi longtemps que l'appli
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CARD);
        db.execSQL(CREATE_USER_CARD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Copie locale uniquement : on peut tout recréer, Firestore fait foi
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CARD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARD);
        onCreate(db);
    }
}
