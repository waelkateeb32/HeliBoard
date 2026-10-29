package helium314.keyboard

import helium314.keyboard.keyboard.internal.keyboard_parser.LayoutParser
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhoneLayoutsTest {
    private fun load(path: String): String {
        val text = File("src/main/assets/layouts/$path").readText()
        // LayoutParser only treats built-in layouts as json if content starts with '['
        assertTrue(text.startsWith("["), "$path must start with '[' to be parsed as json")
        return text
    }

    @Test fun qwertyPhoneParses() {
        val rows = LayoutParser.parseJsonString(load("main/qwerty_phone.json"))
        assertEquals(listOf(10, 9, 7), rows.map { it.size })
    }

    @Test fun arabicPhoneParses() {
        val rows = LayoutParser.parseJsonString(load("main/arabic_phone.json"))
        assertEquals(listOf(11, 11, 10), rows.map { it.size })
    }

    @Test fun functionalPhoneParses() {
        val rows = LayoutParser.parseJsonString(load("functional/functional_keys_phone.json"))
        assertEquals(listOf(3, 6), rows.map { it.size })
    }
}
