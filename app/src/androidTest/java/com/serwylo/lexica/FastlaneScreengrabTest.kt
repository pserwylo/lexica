package com.serwylo.lexica


import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.serwylo.lexica.db.Database
import com.serwylo.lexica.db.GameMode
import com.serwylo.lexica.lang.EnglishUS
import com.serwylo.lexica.lang.Japanese
import com.serwylo.lexica.lang.Language
import com.serwylo.lexica.lang.LanguageLabel
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

/**
 * The current intention of this test is to be run manually before a release, rather than via
 * CI on every commit. Once it tests something more meaningful than starting a game and ending it
 * (e.g. simulating users dragging their fingers on a real board, selecting real words) then it
 * should probably be promoted to CI.
 *
 * To be run on at least emulator 29 (e.g. because there are some system strings, such as the action
 * menu overflow "more options" text which may be specific to this version.
 */
@LargeTest
class FastlaneScreengrabTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainMenuActivity::class.java, true, false)

    /**
     * https://stackoverflow.com/a/42908995/2391921 (for room database initialization
     */
    @Before
    fun setup() {
        mActivityTestRule.launchActivity(null)
    }

    @Test
    fun screenshots() {


        fromSplashSwitchTheme(ThemeManager.THEME_LIGHT)
        fromSplashChooseLeicon()

        val language = Util.findBestMatchOrNull(mActivityTestRule.activity.resources.configuration.locale, Language.allLanguages.values) ?: EnglishUS()
        selectLanguage(language)

        fromSplashSwitchGameMode(GameMode.Type.SPRINT)

        Screengrab.screenshot("01_main_menu_light")

        fromSplashStartNewGame()

        Screengrab.screenshot("02_game_light")

        fromGameEndGame()
        fromScoreSelectMissedWords()

        Screengrab.screenshot("03_missed_words_light")

        fromScoreSelectBack()
        fromSplashChooseGameMode()

        Screengrab.screenshot("04_game_modes")

        pressBack()
        fromSplashChooseLeicon()

        Screengrab.screenshot("04_lexicons")

        pressBack()
        fromSplashShowSettings()

        Screengrab.screenshot("04_preferences")

        pressBack()
        fromSplashSwitchTheme(ThemeManager.THEME_DARK)
        fromSplashChooseLeicon()
        selectLanguage(Japanese())

        Screengrab.screenshot("05_main_menu_dark")

        fromSplashStartNewGame()

        Screengrab.screenshot("06_game_dark")
    }

    private fun fromSplashSwitchGameMode(type: GameMode.Type) {
        fromSplashChooseGameMode()
        selectGameModeItem { it.type == type }
        pressBack()
    }

    private fun fromSplashSwitchTheme(theme: String) {
        fromSplashShowSettings()
        fromSettingsOpenThemeChooser()
        fromSettingsSelectTheme(theme)
        pressBack()
    }

    private fun fromSettingsSelectTheme(theme: String) {
        val themes = mActivityTestRule.activity.applicationContext.resources.getStringArray(R.array.theme_choices_entryvalues)
        val themeIndex = themes.indexOf(theme)

        onData(anything())
                .inAdapterView(withId(R.id.select_dialog_listview))
                .atPosition(themeIndex)
                .perform(click())
    }

    private fun fromSettingsOpenThemeChooser() {
        val recyclerView4 = onView(
                allOf(withId(R.id.recycler_view),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)))
        recyclerView4.perform(actionOnItemAtPosition<ViewHolder>(1, click()))
    }

    private fun fromSplashShowSettings() {
        clickId(R.id.preferences)
    }

    private fun fromScoreSelectBack() {
        clickId(R.id.back_button)
    }

    private fun fromScoreSelectMissedWords() {
        clickId(R.id.missed_words_button)
    }

    private fun fromGameEndGame() {
        clickId(R.id.end_game)
    }

    private fun fromSplashStartNewGame() {
        clickId(R.id.new_game)
    }

    private fun selectLanguage(language: Language) {
        val languageNames = LanguageLabel.getAllLanguagesSorted(mActivityTestRule.activity.applicationContext).map { it.name }
        val languageIndex = languageNames.indexOf(language.name)

        onView(withId(R.id.lexicon_list))
                .perform(actionOnItemAtPosition<ChooseLexiconActivity.ViewHolder>(languageIndex, click()))
    }

    private fun fromSplashChooseLeicon() {
        clickId(R.id.language_button)
    }

    private fun selectGameModeItem(filter: (GameMode) -> Boolean) {
        val db = Database.get(mActivityTestRule.activity.applicationContext)
        val gameModeIndex = db.gameModeDao()
                .getAllGameModesSynchronous()
                .indexOfFirst(filter)

        onView(withId(R.id.game_mode_list))
                .perform(actionOnItemAtPosition<ChooseLexiconActivity.ViewHolder>(gameModeIndex, click()))
    }

    private fun fromSplashChooseGameMode() {
        clickId(R.id.game_mode_button)
    }

    private fun clickId(@IdRes id: Int) {
        onView(allOf(withId(id), isDisplayed())).perform(click())
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    companion object {
        @get:ClassRule
        @JvmStatic
        val localeTestRule = LocaleTestRule()
    }
}
