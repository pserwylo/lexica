package com.serwylo.lexica.lang;

import java.util.Locale;

public class EnglishGBTop1000 extends English {
    @Override
    public String getName() {
        return "en_GB_top1000";
    }

    @Override
    public Locale getLocale() {
        return new Locale("en", "UK");
    }
}
