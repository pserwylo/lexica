package com.serwylo.lexica.lang;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Swedish extends Language {

    private static Map<String, Integer> letterPoints = new HashMap<>();

    static {

        letterPoints.put("é", 1);
        letterPoints.put("-", 1);
        letterPoints.put("q", 1);
        letterPoints.put("w", 1);

        letterPoints.put("a", 1);
        letterPoints.put("r", 1);
        letterPoints.put("s", 1);
        letterPoints.put("t", 1);
        letterPoints.put("e", 1);
        letterPoints.put("n", 1);
        letterPoints.put("d", 1);
        letterPoints.put("i", 1);
        letterPoints.put("l", 1);

        letterPoints.put("o", 2);
        letterPoints.put("g", 2);
        letterPoints.put("k", 2);
        letterPoints.put("m", 2);
        letterPoints.put("h", 2);

        letterPoints.put("f", 3);
        letterPoints.put("v", 3);
        letterPoints.put("ä", 3);

        letterPoints.put("u", 4);
        letterPoints.put("b", 4);
        letterPoints.put("p", 4);
        letterPoints.put("ö", 4);
        letterPoints.put("å", 4);

        letterPoints.put("j", 7);
        letterPoints.put("y", 7);

        letterPoints.put("c", 8);
        letterPoints.put("x", 8);

        letterPoints.put("z", 10);
    }

    @Override
    public boolean isBeta() {
        return true;
    }

    @Override
    public Locale getLocale() {
        return new Locale("sv");
    }

    @Override
    public String getName() {
        return "sv";
    }

    @Override
    public String toDisplay(String value) {
        return value.toUpperCase(getLocale());
    }

    @Override
    public String applyMandatorySuffix(String value) {
        return value;
    }

    @Override
    protected Map<String, Integer> getLetterPoints() {
        return letterPoints;
    }
}
