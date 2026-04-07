package com.commerceos.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");

    private SlugUtils() {}

    public static String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = WHITESPACE.matcher(normalized).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        slug = MULTI_DASH.matcher(slug).replaceAll("-");
        return slug.toLowerCase(Locale.ROOT).replaceAll("^-|-$", "");
    }
}
