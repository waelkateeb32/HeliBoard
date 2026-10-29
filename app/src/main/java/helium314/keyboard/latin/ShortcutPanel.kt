// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin

import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.UserDictionary
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.settings.Settings

/** In-keyboard panel to add a personal-dictionary shortcut. Fields are plain TextViews fed by key events. */
class ShortcutPanel(private val ime: LatinIME, root: View) {
    private val panel: View? = root.findViewById(R.id.shortcut_panel)
    private val views: List<TextView> = listOfNotNull(root.findViewById(R.id.shortcut_abbrev), root.findViewById(R.id.shortcut_full))
    private val buffers = ShortcutBuffers()
    private val handler = Handler(Looper.getMainLooper())
    private var caretOn = true
    private val textColor = Settings.getValues().mColors.get(ColorType.KEY_TEXT)
    private val accent = Settings.getValues().mColors.get(ColorType.ACTION_KEY_BACKGROUND)
    private val blink = object : Runnable {
        override fun run() { caretOn = !caretOn; render(); handler.postDelayed(this, 530) }
    }

    init {
        views.forEachIndexed { i, v ->
            v.setTextColor(textColor)
            v.setHintTextColor((textColor and 0x00ffffff) or 0x80000000.toInt())
            v.setOnClickListener { buffers.active = i; caretOn = true; render() }
        }
        root.findViewById<Button>(R.id.shortcut_cancel)?.apply { setTextColor(textColor); setOnClickListener { hide(true) } }
        root.findViewById<Button>(R.id.shortcut_save)?.apply { setTextColor(textColor); setOnClickListener { save() } }
    }

    private val isShowing get() = panel?.visibility == View.VISIBLE

    val visibleHeight: Int get() = if (isShowing) panel!!.height else 0

    fun toggle() { if (isShowing) hide(true) else show() }

    private fun show() {
        val p = panel ?: return
        ime.inputLogicForPanel.mConnection.finishComposingText()
        buffers.active = 0
        p.visibility = View.VISIBLE
        caretOn = true; render()
        handler.removeCallbacks(blink); handler.postDelayed(blink, 530)
        p.requestLayout()
    }

    fun hide(clear: Boolean) {
        if (clear) buffers.clear()
        handler.removeCallbacks(blink)
        if (!isShowing) return
        panel!!.visibility = View.GONE
        panel.requestLayout()
    }

    fun handleCode(code: Int): Boolean {
        if (!isShowing) return false
        val cm = ime.getSystemService(android.content.ClipboardManager::class.java)
        when (buffers.handleCode(code)) {
            ShortcutBuffers.Action.NONE -> return false
            ShortcutBuffers.Action.CONSUMED -> {}
            ShortcutBuffers.Action.PASTE -> cm?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)
                ?.coerceToText(ime)?.toString()?.let { buffers.handleText(it) }
            ShortcutBuffers.Action.COPY -> buffers.activeText().takeIf { it.isNotEmpty() }
                ?.let { cm?.setPrimaryClip(android.content.ClipData.newPlainText("", it)) }
            ShortcutBuffers.Action.CUT -> buffers.cut().takeIf { it.isNotEmpty() }
                ?.let { cm?.setPrimaryClip(android.content.ClipData.newPlainText("", it)) }
        }
        caretOn = true; render(); return true
    }

    fun handleText(text: String?): Boolean {
        if (!isShowing || text == null) return false
        buffers.handleText(text); caretOn = true; render(); return true
    }

    private fun render() {
        val d = ime.resources.displayMetrics.density
        views.forEachIndexed { i, v ->
            val activeField = i == buffers.active
            val t = buffers.fields[i].toString()
            v.text = if (activeField && (t.isNotEmpty() || caretOn)) t + (if (caretOn) "|" else " ") else t
            v.background = GradientDrawable().apply {
                cornerRadius = 8 * d
                setStroke(((if (activeField) 2 else 1) * d).toInt(), if (activeField) accent else (textColor and 0x00ffffff) or 0x40000000)
            }
        }
    }

    private fun save() {
        val s = buffers.fields[0].toString().trim()
        val w = buffers.fields[1].toString().trim()
        if (s.isEmpty() || w.isEmpty()) {
            Toast.makeText(ime, R.string.shortcut_panel_missing, Toast.LENGTH_SHORT).show(); return
        }
        runCatching { UserDictionary.Words.addWord(ime, w, 250, s, null) }
            .onSuccess { Toast.makeText(ime, R.string.shortcut_panel_saved, Toast.LENGTH_SHORT).show(); hide(true) }
            .onFailure { Toast.makeText(ime, it.message ?: "error", Toast.LENGTH_SHORT).show() }
    }
}
