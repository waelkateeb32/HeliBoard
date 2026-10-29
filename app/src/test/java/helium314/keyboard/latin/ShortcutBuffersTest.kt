package helium314.keyboard.latin

import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode
import helium314.keyboard.latin.ShortcutBuffers.Action
import org.junit.Assert.assertEquals
import org.junit.Test

class ShortcutBuffersTest {
    @Test fun routing() {
        val b = ShortcutBuffers()
        "brb".forEach { b.handleCode(it.code) }
        b.handleCode(KeyCode.DELETE); b.handleCode('b'.code)
        b.handleCode('\n'.code)
        "مرحبا 😀".codePoints().forEach { b.handleCode(it) }
        b.handleCode(KeyCode.DELETE)
        assertEquals(Action.NONE, b.handleCode(KeyCode.SHIFT))
        assertEquals(Action.NONE, b.handleCode(KeyCode.CLIPBOARD)) // history view must still open
        assertEquals("brb", b.fields[0].toString())
        assertEquals("مرحبا ", b.fields[1].toString())
        assertEquals(1, b.active)
    }

    @Test fun pasteAndEdit() {
        val b = ShortcutBuffers()
        assertEquals(Action.PASTE, b.handleCode(KeyCode.CLIPBOARD_PASTE))
        b.handleText("a\nb")
        assertEquals("a b", b.fields[0].toString())
        b.active = 1
        b.handleText("line1\nline2")
        assertEquals("line1\nline2", b.fields[1].toString())
        assertEquals(Action.CONSUMED, b.handleCode(KeyCode.ARROW_LEFT))
        b.handleCode(KeyCode.UNDO)
        assertEquals("", b.fields[1].toString())
        b.handleText("x y")
        b.handleCode(KeyCode.DELETE_WORD)
        assertEquals("x ", b.fields[1].toString())
        assertEquals(Action.CUT, b.handleCode(KeyCode.CLIPBOARD_CUT))
        assertEquals("x ", b.cut()); assertEquals("", b.activeText())
    }
}
