package helium314.keyboard.latin

import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShortcutBuffersTest {
    @Test fun routing() {
        val b = ShortcutBuffers()
        "brb".forEach { b.handleCode(it.code) }
        b.handleCode(KeyCode.DELETE); b.handleCode('b'.code)
        b.handleCode('\n'.code)
        "مرحبا 😀".codePoints().forEach { b.handleCode(it) }
        b.handleCode(KeyCode.DELETE)
        assertFalse(b.handleCode(KeyCode.SHIFT))
        assertEquals("brb", b.fields[0].toString())
        assertEquals("مرحبا ", b.fields[1].toString())
        assertEquals(1, b.active)
    }
}
