package se.gory_moon.vctweaker.util;


import net.minecraft.client.resources.I18n;

import java.util.IllegalFormatException;

public final class Translator {
    private Translator() {
    }

    public static String translateToLocal(String key) {
        if (I18n.hasKey(key)) {
            return I18n.format(key);
        } else {
            return key;
        }
    }

    public static String translateToLocalFormatted(String key, Object... format) {
        String s = translateToLocal(key);
        try {
            return String.format(s, format);
        } catch (IllegalFormatException e) {
            String errorMessage = "Format error: " + s;
            Log.error(errorMessage, e);
            return errorMessage;
        }
    }
}