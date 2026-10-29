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
        // physical order (functional rows are not mirrored for RTL): !#1 | ! | space | globe | . | action
        val labels = rows[1].map { (it as? helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyData)?.label }
        assertEquals(listOf(null, "!", "space", "language_switch", "period", "action"), labels)
    }

    @Test fun westernDigitsByDefault() {
        // localized (Arabic-Indic) number row is off by default -> 1234567890 on Arabic keyboard
        kotlin.test.assertFalse(helium314.keyboard.latin.settings.Defaults.PREF_LOCALIZED_NUMBER_ROW)
        val row = LayoutParser.parseJsonString(load("number_row/number_row.json")).first()
        assertEquals(10, row.size)
    }
}
