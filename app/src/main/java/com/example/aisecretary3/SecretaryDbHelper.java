package com.example.aisecretary3;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SecretaryDbHelper extends SQLiteOpenHelper {

    // New database file specifically for version 3 to prevent cross-app collisions
    public static final String DATABASE_NAME = "SecretaryData3.db";
    private static final int DATABASE_VERSION = 2; // Fresh start for AISecretary3 ,to 2 for deleting old one 2606230152

    // Column Definitions (Explicitly tracing origins)
    public static final String COL_TIMESTAMP = "custom_timestamp"; // YYYY-MM-DD hh:mm:ss
    public static final String COL_ROLE = "role";                // The persona string
    public static final String COL_AVATAR = "avatar_used";        // Active avatar graphic tracking
    public static final String COL_MODEL_USED = "model_used";    // Ollama model name tracking
    public static final String COL_PROMPT = "prompt";            // Input query
    public static final String COL_RESPONSE = "response";        // AI output
    public static final String COL_AUDIO_IN = "audio_in";        // Mic BLOB
    public static final String COL_TPS = "tps";                  // Speed metric
    public static final String COL_STATUS = "status";            // STABLE/HOT

    public SecretaryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Deterministic Table Creation - Complete Traceability Matrix
        db.execSQL("CREATE TABLE chat_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TIMESTAMP + " TEXT, " +
                COL_ROLE + " TEXT, " +
                COL_AVATAR + " TEXT, " +
                COL_MODEL_USED + " TEXT, " +
                COL_PROMPT + " TEXT, " +
                COL_RESPONSE + " TEXT, " +
                COL_AUDIO_IN + " BLOB, " +
                COL_TPS + " REAL, " +
                COL_STATUS + " TEXT)");
    }

    /**
     * THE LOGIC ENGINE: Captures the state variables from MainActivity at runtime.
     * Hard-stamps standard industrial time notation: YYYY-MM-DD hh:mm:ss
     */
    public void saveLog(String role, String activeAvatar, String selectedModel, String p, String r, byte[] audio, double tps, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // 1. Generate the standard deterministic timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = sdf.format(new java.util.Date());

        // 2. Load fields into the row matrix
        cv.put(COL_TIMESTAMP, currentDateTime);
        cv.put(COL_ROLE, role);
        cv.put(COL_AVATAR, activeAvatar);
        cv.put(COL_MODEL_USED, selectedModel);
        cv.put(COL_PROMPT, p);
        cv.put(COL_RESPONSE, r);
        cv.put(COL_AUDIO_IN, audio);
        cv.put(COL_TPS, tps);
        cv.put(COL_STATUS, stat);

        db.insert("chat_history", null, cv);
    }

    //@Override
    //public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since version is 1 and database name is fresh, no upgrade routine needed yet.
    //}

    public void wipeHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete("chat_history", null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS chat_history");
        onCreate(db);
    }
}