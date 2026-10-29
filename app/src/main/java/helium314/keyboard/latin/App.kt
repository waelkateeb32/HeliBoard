// SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
package helium314.keyboard.latin

import android.app.Application
import android.os.Build
import helium314.keyboard.keyboard.emoji.SupportedEmojis
import helium314.keyboard.latin.define.DebugFlags
import helium314.keyboard.latin.dictionary.ExpandableDictionaryUtils
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.FoldableUtils
import helium314.keyboard.latin.utils.LayoutUtilsCustom
import helium314.keyboard.latin.utils.Log
import helium314.keyboard.latin.utils.SubtypeSettings
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.latin.utils.upgradeToolbarPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DebugFlags.init(this)
        FoldableUtils.init(this)
        Settings.init(this)
        SubtypeSettings.init(this)

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch { // do some uncritical work in background for faster startup
            SupportedEmojis.load(this@App)
            ExpandableDictionaryUtils.initUpToDateDicts(this@App)
            LayoutUtilsCustom.removeMissingLayouts(this@App)
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            @Suppress("DEPRECATION")
            Log.i(
                "startup", "Starting ${applicationInfo.processName} version ${packageInfo.versionName} (${
                    packageInfo.versionCode
                }) on Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
            )
        }

        RichInputMethodManager.init(this)
        checkVersionUpgrade(this)
        applyCustomDefaultsOnce(this)
        applyCustomDefaultsV7Once(this)
        if (BuildConfig.DEBUG) // do this on every debug apk start because we may work on adding a new toolbar key
            upgradeToolbarPrefs(prefs())
        transferOldPinnedClips(this) // todo: remove in a few months, maybe end 2026
        app = this
        Defaults.initDynamicDefaults(this)
    }

    companion object {
        // used so JniUtils can access application once
        private var app: App? = null
        fun getApp(): App? {
            val application = app
            app = null
            return application
        }
    }
}

/** custom fork: reset look/feel prefs once so new Samsung-like defaults apply over older installs */
fun applyCustomDefaultsOnce(context: android.content.Context) {
    val p = context.prefs()
    val flag = "custom_defaults_v6_applied"
    if (p.getBoolean(flag, false)) return
    val e = p.edit()
    listOf(Settings.PREF_VIBRATE_ON, Settings.PREF_POPUP_ON, Settings.PREF_VIBRATION_DURATION_SETTINGS,
        Settings.PREF_KEY_LONGPRESS_TIMEOUT, Settings.PREF_THEME_COLORS, Settings.PREF_THEME_COLORS_NIGHT,
        Settings.PREF_THEME_STYLE, Settings.PREF_THEME_KEY_BORDERS, Settings.PREF_LOCALIZED_NUMBER_ROW,
        Settings.PREF_SHOW_NUMBER_ROW).forEach { e.remove(it) }
    e.putBoolean(flag, true).apply()
}

/** custom fork v7: reset pinned toolbar keys once so the new ADD_SHORTCUT key appears pinned on upgrade */
fun applyCustomDefaultsV7Once(context: android.content.Context) {
    val p = context.prefs()
    val flag = "custom_defaults_v7_applied"
    if (p.getBoolean(flag, false)) return
    p.edit().remove(Settings.PREF_PINNED_TOOLBAR_KEYS).putBoolean(flag, true).apply()
}
