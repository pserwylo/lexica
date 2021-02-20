package com.serwylo.lexica.lang;

import java.util.Locale;

public class EnglishUSAtoK extends English {
    @Override
    public String getName() {
        return "en_US_AtoK";
    }

    @Override
    public Locale getLocale() {
        return new Locale("en", "US");
    }
}
