package br.com.nivo.volume;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    static final String NAME = "nivo_settings";
    static final int BG = 0xFF070A10;
    static final int SURFACE = 0xFF111824;
    static final int OUTLINE = 0xFF263244;
    static final int TEXT = 0xFFF2F7FD;
    static final int MUTED = 0xFF91A3B7;
    static final int[] COLORS = {
        0xFF65DDFD, 0xFF528DFF, 0xFFB39AFF, 0xFFFFAD73, 0xFF75E7B6
    };
    static SharedPreferences get(Context c) { return c.getSharedPreferences(NAME, Context.MODE_PRIVATE); }
    static int integer(Context c, String key, int fallback) { return get(c).getInt(key, fallback); }
    static boolean flag(Context c, String key, boolean fallback) { return get(c).getBoolean(key, fallback); }
    static void put(Context c, String key, int v) { get(c).edit().putInt(key, v).apply(); }
    static void put(Context c, String key, boolean v) { get(c).edit().putBoolean(key, v).apply(); }
    static int accent(Context c) {
        int index = Math.max(0, Math.min(COLORS.length - 1, integer(c, "accent", 0)));
        return COLORS[index];
    }
}
