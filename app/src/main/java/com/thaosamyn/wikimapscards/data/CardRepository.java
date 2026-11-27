package com.thaosamyn.wikimapscards.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.thaosamyn.wikimapscards.model.Card;
import com.thaosamyn.wikimapscards.model.UserCard;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Point d'entrée des écrans vers les cartes locales.
 * Les requêtes SQLite tournent sur un thread de fond, le résultat revient sur le thread UI.
 */
public class CardRepository {

    /** Résultat d'une requête, appelé sur le thread UI. */
    public interface Callback<T> {
        void onResult(T result);
    }

    // Temporaire tant que l'auth n'est pas mergée : à remplacer par FirebaseAuth.getInstance().getUid()
    public static final String LOCAL_UID = "local";

    private static CardRepository instance;

    private final CardDao dao;
    // Un seul thread : les écritures s'exécutent dans l'ordre, sans accès concurrent à la base
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private CardRepository(Context context) {
        dao = new CardDao(new DbHelper(context));
    }

    /** Une seule instance pour toute l'appli (une seule connexion SQLite). */
    public static synchronized CardRepository getInstance(Context context) {
        if (instance == null) {
            instance = new CardRepository(context);
        }
        return instance;
    }

    public void addCard(Card card, String userUid, Long unlockedFrom, Runnable onDone) {
        run(() -> {
            dao.insertOrIncrement(card, userUid, unlockedFrom);
            return null;
        }, result -> {
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void owns(String userUid, long pageId, Callback<Boolean> callback) {
        run(() -> dao.owns(userUid, pageId), callback);
    }

    public void getOwnedPlaceIds(String userUid, Callback<Set<Long>> callback) {
        run(() -> dao.getOwnedPlaceIds(userUid), callback);
    }

    public void getPlaces(String userUid, Callback<List<UserCard>> callback) {
        run(() -> dao.getPlaces(userUid), callback);
    }

    public void getCardsUnlockedFrom(String userUid, long placeId, Callback<List<UserCard>> callback) {
        run(() -> dao.getCardsUnlockedFrom(userUid, placeId), callback);
    }

    /** Exécute la tâche en fond puis renvoie son résultat au thread UI. */
    private <T> void run(Callable<T> task, Callback<T> callback) {
        executor.execute(() -> {
            try {
                T result = task.call();
                mainHandler.post(() -> callback.onResult(result));
            } catch (Exception e) {
                Log.e("CardRepository", "Erreur base locale", e);
            }
        });
    }
}
