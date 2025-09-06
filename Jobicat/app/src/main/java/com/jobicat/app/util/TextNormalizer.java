package com.jobicat.app.util;

import java.text.Normalizer;
import java.util.Locale;

public class TextNormalizer {
    public static String normalize(String input) {
        if (input == null) return "";
        String lower = input.toLowerCase(Locale.ROOT).trim();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}