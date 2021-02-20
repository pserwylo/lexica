package com.serwylo.lexica.lang

import android.content.Context
import android.util.Log

object LanguageLabel {

    private const val TAG = "LanguageLabel"

    @JvmStatic fun getLabel(context: Context, language: Language): String {

        val prefName = "pref_dict_" + language.name

        val resId = context.resources.getIdentifier(prefName, "string", context.packageName)
        if (resId <= 0) {
            Log.e(TAG, "Language " + language.name + " does not have a corresponding R.string.pref_dict_" + language.name + " value, defaulting to " + language.name)
            return language.name
        }

        return context.getString(resId)

    }

    @JvmStatic fun getAllLanguagesSorted(context: Context): List<Language> {

        // This and teh expression below is used to provide a list of languages which is sorted
        // by their internationalised name.
        val languagesWithLabels = Language.allLanguages
                .values
                .associateBy { getLabel(context, it) }

        return languagesWithLabels
                .keys
                .sorted()
                .map { languagesWithLabels[it] ?: error("Could not get language from map we just created, which is weird.") }

    }
}