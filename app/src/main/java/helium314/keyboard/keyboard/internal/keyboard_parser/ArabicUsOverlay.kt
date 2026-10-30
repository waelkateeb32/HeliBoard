// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard.internal.keyboard_parser

import helium314.keyboard.keyboard.KeyboardLayoutSet
import helium314.keyboard.keyboard.KeyboardSwitcher

/** custom fork: US QWERTY keyboard showing Arabic-101 letters as key backgrounds; toggled by long-press on the globe key */
object ArabicUsOverlay {
    const val LAYOUT = "arabic_us_background"
    @JvmField var active = false

    @JvmStatic fun reload() {
        KeyboardLayoutSet.onKeyboardThemeChanged() // clears keyboard cache
        KeyboardSwitcher.getInstance().reloadKeyboard()
    }
}
