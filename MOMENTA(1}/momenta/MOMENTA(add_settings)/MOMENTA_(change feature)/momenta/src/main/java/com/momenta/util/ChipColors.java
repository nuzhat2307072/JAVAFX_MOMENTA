package com.momenta.util;

/**
 * Gives category/tag chips a bit of visual variety instead of every chip
 * being the same flat blue: the same tag always gets the same color
 * (hashed, not random), so it stays visually consistent across screens.
 */
public class ChipColors {

    private static final String[] STYLE_CLASSES = {
            "chip-blue", "chip-teal", "chip-purple", "chip-orange", "chip-pink", "chip-green"
    };

    private static final String[] HEX_COLORS = {
            "#1C6FEB", "#0D9488", "#7C3AED", "#EA580C", "#DB2777", "#16A34A"
    };

    public static String styleClassFor(String text) {
        return STYLE_CLASSES[indexFor(text)];
    }

    /** Matching hex color for the same text, for inline styles (e.g. a ProgressBar's -fx-accent). */
    public static String hexFor(String text) {
        return HEX_COLORS[indexFor(text)];
    }

    private static int indexFor(String text) {
        if (text == null || text.isEmpty()) return 0;
        return Math.abs(text.toLowerCase().hashCode()) % STYLE_CLASSES.length;
    }
}
