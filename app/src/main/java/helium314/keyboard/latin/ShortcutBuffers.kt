// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin

import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode

/** Pure text buffers for the add-shortcut panel; key events are routed here instead of the app. */
class ShortcutBuffers {
    val fields = arrayOf(StringBuilder(), StringBuilder())
    var active = 0

    /** @return true if the code was consumed */
    fun handleCode(code: Int): Boolean = when {
        code == KeyCode.DELETE -> { deleteLast(); true }
        code == '\n'.code -> { active = (active + 1) % 2; true }
        code > 0 -> { fields[active].appendCodePoint(code); true }
        else -> false
    }

    fun handleText(text: String) { fields[active].append(text) }

    private fun deleteLast() {
        val sb = fields[active]
        if (sb.isEmpty()) return
        val cp = sb.codePointBefore(sb.length)
        sb.setLength(sb.length - Character.charCount(cp))
    }

    fun clear() { fields.forEach { it.setLength(0) }; active = 0 }
}
