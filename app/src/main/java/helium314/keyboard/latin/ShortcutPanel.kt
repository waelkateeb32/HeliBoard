// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin

import android.graphics.Color
import android.provider.UserDictionary
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import helium314.keyboard.latin.settings.Settings

/** In-keyboard panel to add a personal-dictionary shortcut. While shown, the keyboard types into the selected field. */
class ShortcutPanel(private val ime: LatinIME, root: View) {
    private val panel: View? = root.findViewById(R.id.shortcut_panel)
    private val abbrev: EditText? = root.findViewById(R.id.shortcut_abbrev)
    private val full: EditText? = root.findViewById(R.id.shortcut_full)
    private var active: EditText? = null
    private var ic: InputConnection? = null

    init {
        val colors = Settings.getValues().mColors
        val textColor = colors.get(helium314.keyboard.latin.common.ColorType.KEY_TEXT)
        listOfNotNull(abbrev, full).forEach { f ->
            f.setTextColor(textColor)
            f.setHintTextColor((textColor and 0x00ffffff) or 0x80000000.toInt())
            f.showSoftInputOnFocus = false
            f.setOnClickListener { select(f) }
        }
        root.findViewById<Button>(R.id.shortcut_cancel)?.apply { setTextColor(textColor); setOnClickListener { hide() } }
        root.findViewById<Button>(R.id.shortcut_save)?.apply { setTextColor(textColor); setOnClickListener { save() } }
    }

    val inputConnection: InputConnection? get() = if (panel?.visibility == View.VISIBLE) ic else null

    fun toggle() { if (panel?.visibility == View.VISIBLE) hide() else show() }

    private fun show() {
        val p = panel ?: return
        ime.inputLogicForPanel.mConnection.finishComposingText()
        abbrev?.setText(""); full?.setText("")
        p.visibility = View.VISIBLE
        select(abbrev ?: return)
    }

    private fun select(f: EditText) {
        if (active != null) ime.inputLogicForPanel.mConnection.finishComposingText()
        active?.alpha = 0.6f
        active = f
        f.alpha = 1f
        f.setSelection(f.text.length)
        ic = f.onCreateInputConnection(EditorInfo().apply { inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS })
        resetCaches(f.text.length)
    }

    private fun resetCaches(pos: Int) {
        runCatching { ime.inputLogicForPanel.mConnection.resetCachesUponCursorMoveAndReturnSuccess(pos, pos, false) }
    }

    fun hide() {
        if (panel?.visibility != View.VISIBLE) return
        ime.inputLogicForPanel.mConnection.finishComposingText()
        panel.visibility = View.GONE
        active = null; ic = null
        val ei = ime.currentInputEditorInfo
        resetCaches(ei?.initialSelStart?.coerceAtLeast(0) ?: 0)
    }

    private fun save() {
        ime.inputLogicForPanel.mConnection.finishComposingText()
        val s = abbrev?.text?.toString()?.trim().orEmpty()
        val w = full?.text?.toString()?.trim().orEmpty()
        if (s.isEmpty() || w.isEmpty()) {
            Toast.makeText(ime, R.string.shortcut_panel_missing, Toast.LENGTH_SHORT).show(); return
        }
        runCatching { UserDictionary.Words.addWord(ime, w, 250, s, null) }
            .onSuccess { Toast.makeText(ime, R.string.shortcut_panel_saved, Toast.LENGTH_SHORT).show(); hide() }
            .onFailure { Toast.makeText(ime, it.message ?: "error", Toast.LENGTH_SHORT).show() }
    }
}
