// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin

import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode

/** Pure text buffers for the add-shortcut panel; key events, pastes and commits are routed here instead of the app. */
class ShortcutBuffers {
    val fields = arrayOf(StringBuilder(), StringBuilder())
    var active = 0
    private val undo = ArrayDeque<Pair<Int, String>>()

    /** Result for codes that need platform access (clipboard) */
    enum class Action { NONE, CONSUMED, PASTE, COPY, CUT }

    fun handleCode(code: Int): Action = when (code) {
        KeyCode.DELETE -> { snapshot(); deleteLast(); Action.CONSUMED }
        KeyCode.DELETE_WORD -> { snapshot(); deleteWord(); Action.CONSUMED }
        '\n'.code -> { active = (active + 1) % 2; Action.CONSUMED }
        KeyCode.CLIPBOARD_PASTE -> Action.PASTE
        KeyCode.CLIPBOARD_COPY, KeyCode.CLIPBOARD_COPY_ALL, KeyCode.CLIPBOARD_SELECT_ALL -> Action.COPY
        KeyCode.CLIPBOARD_CUT -> Action.CUT
        KeyCode.UNDO -> { undoLast(); Action.CONSUMED }
        KeyCode.REDO, KeyCode.CLIPBOARD_SELECT_WORD, KeyCode.ARROW_LEFT, KeyCode.ARROW_RIGHT, KeyCode.ARROW_UP, KeyCode.ARROW_DOWN,
        KeyCode.WORD_LEFT, KeyCode.WORD_RIGHT, KeyCode.MOVE_START_OF_LINE, KeyCode.MOVE_END_OF_LINE,
        KeyCode.MOVE_START_OF_PAGE, KeyCode.MOVE_END_OF_PAGE, KeyCode.PAGE_UP, KeyCode.PAGE_DOWN -> Action.CONSUMED
        else -> if (code > 0) { snapshot(); fields[active].appendCodePoint(code); Action.CONSUMED } else Action.NONE
    }

    /** Insert text (paste, clipboard history, suggestion). Newlines become spaces in the shortcut field. */
    fun handleText(text: String) {
        snapshot()
        fields[active].append(if (active == 0) text.replace(Regex("\\s*\\R\\s*"), " ") else text)
    }

    fun cut(): String { snapshot(); val t = fields[active].toString(); fields[active].setLength(0); return t }
    fun activeText() = fields[active].toString()

    private fun snapshot() { undo.addLast(active to fields[active].toString()); if (undo.size > 50) undo.removeFirst() }
    private fun undoLast() {
        val (i, t) = undo.removeLastOrNull() ?: return
        fields[i].setLength(0); fields[i].append(t); active = i
    }

    private fun deleteLast() {
        val sb = fields[active]
        if (sb.isEmpty()) return
        val cp = sb.codePointBefore(sb.length)
        sb.setLength(sb.length - Character.charCount(cp))
    }

    private fun deleteWord() {
        val sb = fields[active]
        var n = sb.length
        while (n > 0 && sb[n - 1].isWhitespace()) n--
        while (n > 0 && !sb[n - 1].isWhitespace()) n--
        sb.setLength(n)
    }

    fun clear() { fields.forEach { it.setLength(0) }; active = 0; undo.clear() }
}
