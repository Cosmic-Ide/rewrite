/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.rewrite.common

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * A utility object to access shared preferences easily.
 */
object Prefs {

    private lateinit var prefs: SharedPreferences

    /**
     * Initializes shared preferences.
     * @param context The context of the application.
     */
    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    val appTheme: String
        get() = prefs.getString("app_theme", "auto") ?: "auto"

    val appAccent: String
        get() = prefs.getString("app_accent", "default") ?: "default"

    val useFastJarFs: Boolean
        get() = prefs.getBoolean("use_fastjarfs", true)

    val stickyScroll: Boolean
        get() = prefs.getBoolean("sticky_scroll", false)

    val useLigatures: Boolean
        get() = prefs.getBoolean("font_ligatures", true)

    val wordWrap: Boolean
        get() = prefs.getBoolean("word_wrap", false)

    val scrollbarEnabled: Boolean
        get() = prefs.getBoolean("scrollbar", true)

    val hardwareAcceleration: Boolean
        get() = prefs.getBoolean("hardware_acceleration", true)

    val nonPrintableCharacters: Boolean
        get() = prefs.getBoolean("non_printable_characters", false)

    val ktfmtStyle: String
        get() = prefs.getString("ktfmt_style", "google") ?: "google"

    val googleJavaFormatOptions: Set<String>?
        get() = prefs.getStringSet("google_java_formatter_options", setOf())

    val googleJavaFormatStyle: String
        get() = prefs.getString("google_java_formatter_style", "aosp") ?: "aosp"
    val lineNumbers: Boolean
        get() = prefs.getBoolean("line_numbers", true)

    val useSpaces: Boolean
        get() = prefs.getBoolean("use_spaces", false)

    val tabSize: Int
        get() = prefs.getInt("tab_size", 4)

    val bracketPairAutocomplete: Boolean
        get() = prefs.getBoolean("bracket_pair_autocomplete", true)

    val quickDelete: Boolean
        get() = prefs.getBoolean("quick_delete", false)

    val javacFlags: String
        get() = prefs.getString("javac_flags", "") ?: ""

    val compilerJavaVersion: Int
        get() = Integer.parseInt(prefs.getString("java_version", "17") ?: "17")

    val kotlinVersion: String
        get() = prefs.getString("kotlin_version", "2.1") ?: "2.1"

    val useSSVM: Boolean
        get() = prefs.getBoolean("use_ssvm", false)

    val analyticsEnabled: Boolean
        get() = prefs.getBoolean("analytics_preference", true)

    val doubleClickClose: Boolean
        get() = prefs.getBoolean("double_click_close", false)

    val experimentalJavaCompletion: Boolean
        get() = prefs.getBoolean("experimental_java_completion", false)

    val gitUsername: String
        get() = prefs.getString("git_username", "") ?: ""

    val gitEmail: String
        get() = prefs.getString("git_email", "") ?: ""

    val gitApiKey: String
        get() = prefs.getString("git_api_key", "") ?: ""

    val pluginRepository: String
        get() = prefs.getString(
            "plugin_repository",
            "https://raw.githubusercontent.com/Cosmic-IDE/plugins-repo/main/plugins.json"
        ) ?: "https://raw.githubusercontent.com/Cosmic-IDE/plugins-repo/main/plugins.json"

    val editorFontSize: Float
        get() = runCatching {
            prefs.getString("font_size", "14")?.toFloatOrNull()?.coerceIn(1f, 32f) ?: 14f
        }.getOrElse { 16f }
}
