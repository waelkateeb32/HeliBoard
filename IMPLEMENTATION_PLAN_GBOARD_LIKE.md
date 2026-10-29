# HeliBoard → screenshot-matching EN/AR layouts

Upstream: https://github.com/HeliBorg/HeliBoard (main @ 44ddb1ae)
Target fork: waelkateeb32/HeliBoard (**does not exist yet**; API fork returned 403)
Local clone: `/workspace/HeliBoard` on branch `custom-gboard-like-layouts`

## 1. Exact key maps from screenshots

### English (visual LTR = logical)

Toolbar: sparkle | emoji(dot) | translate | clipboard | settings | ⋮

| Row | Keys (primary) | Hints (top-right) |
|-----|----------------|-------------------|
| Numbers | `1 2 3 4 5 6 7 8 9 0` | (none visible) |
| Letters 1 | `Q W E R T Y U I O P` | `+ × ÷ = / \ < > [ ]` |
| Letters 2 | `A S D F G H J K L` (centered) | `! @ # $ % & * ( )` |
| Letters 3 | `[Shift] Z X C V B N M [Delete]` | `- ' " : ; , ?` on letters |
| Bottom | `!#1` \| globe \| `!` \| **English (US)** \| `.` \| **Done** (blue) |

### Arabic (visual LTR as shown; same order as file / QWERTY positions)

Toolbar: same as EN

| Row | Keys visual LTR | Hints |
|-----|-----------------|-------|
| Numbers | `١ ٢ ٣ ٤ ٥ ٦ ٧ ٨ ٩ ٠` | (none visible) |
| Letters 1 | `ض ص ث ق ف غ ع ه خ ح ج` | `+ × ÷ = / _ < > [ ] ~` |
| Letters 2 | `ش س ي ب ل ا ت ن م ك ط` | `! @ # $ % & ^ * - ' "` |
| Letters 3 | `ذ ء ؤ ر ى ة و ز ظ د [Delete]` | _(none on ذ)_ `' " ; : ؟ ! , . \` |
| Bottom | `!#1` \| globe \| `!` \| **العربية** \| `.` \| **Done** (blue) |

Notes:
- Arabic has **no Shift** (matches HeliBoard `NoShiftKey` on `ar` subtypes).
- Letter order already matches stock `arabic.txt` exactly; only hints + chrome differ.
- Spacebar language label is built-in (`English (US)` / `العربية`).
- Period on AR may show a small secondary glyph (screenshot ambiguous; possibly `؟`/`،`/digit) — verify on device.

## 2. Relevant HeliBoard paths

| Area | Paths |
|------|--------|
| Layout docs | `layouts.md` |
| Main layouts | `app/src/main/assets/layouts/main/` (`qwerty.txt`, `arabic.txt`, …) |
| Functional / bottom | `app/src/main/assets/layouts/functional/functional_keys.json` |
| Number row | `app/src/main/assets/layouts/number_row/number_row.json` |
| Locale extras (AR digits, labels, popups) | `app/src/main/assets/locale_key_texts/ar.txt` |
| Subtype registry | `app/src/main/res/xml/method.xml` |
| Themes | `themes-rounded-base.xml`, `themes-rounded-base-border.xml`, `KeyboardTheme.kt` |
| Key radius (10dp) | `res/drawable/btn_keyboard_key_normal_rounded_base_border.xml` |
| Colors / accent (Done) | user theme via `ColorsScreen` / `KeyboardTheme` (`COLOR_ACCENT`, keys, bg) |
| Hints | `PopupKeysUtils.kt` `getHintLabel`; prefs `PREF_SHOW_HINTS`, `PREF_POPUP_KEYS_HINT_ORDER` |
| Appearance prefs | `AppearanceScreen.kt`: style Rounded, key borders, gap scale, height scale |
| Number row pref | `PREF_SHOW_NUMBER_ROW` (default **false**) |

Closest stock layouts:
- EN: `main/qwerty.txt` (letters only; no hint popups)
- AR: `main/arabic.txt` (letters match screenshot; no hint popups)
- Hints example: `main/pcqwerty.json` (different hint set)
- Functional: `functional/functional_keys.json` (comma + optional emoji; symbol label `?123` / AR `٣٢١؟`)

## 3. Step-by-step edit plan

### (a) English layout
1. Add `app/src/main/assets/layouts/main/qwerty_phone.json` (**drafted**) with popup `main` hints from screenshot.
2. Register in `method.xml` for `en_US` (or add as alternate layout `qwerty_phone` + string in `values/strings.xml`), **or** replace `qwerty.txt` contents / convert to json.
3. Prefer new layout name + subtype extra `KeyboardLayoutSet=MAIN:qwerty_phone` so upstream qwerty stays intact.
4. Hint source must prefer **layout** (`PREF_POPUP_KEYS_HINT_ORDER`); with number row ON, disable number-as-hint on letters (`PREF_SHOW_NUMBER_ROW_HINTS` false).

### (b) Arabic layout
1. Add `app/src/main/assets/layouts/main/arabic_phone.json` (**drafted**).
2. Point `ar` subtype in `method.xml` to `MAIN:arabic_phone|SYMBOLS:symbols_arabic,NoShiftKey,...` (keep `NoShiftKey`).
3. Keep `locale_key_texts/ar.txt` `[number_row]` (`١…٠`) and localized number row pref ON.
4. Optionally set `[labels] symbol: !#1` in `ar.txt` if using stock `symbol_alpha` instead of hardcoded functional label.

### (c) Functional / bottom row
1. Add `functional/functional_keys_phone.json` (**drafted**): `!#1` | `language_switch` | `!` | `space` | `period` | `action`.
2. Wire via subtype `FUNCTIONAL:functional_keys_phone` in `method.xml` for EN+AR, **or** replace default functional layout / change app default in `Defaults.kt` `LayoutType` map.
3. Always show globe (do not gate on `languageKeyEnabled`) when matching screenshot.

### (d) Visual theme (closest without pixel-perfect UI rewrite)
**In-app settings (no code) get most of the look:**
- Theme style: **Rounded**
- Key borders: **ON** (10dp radius already)
- Colors (dark): background `#000000`, keys ~`#1F1F1F`, text white, hint gray, accent/action blue for Done
- Number row: **ON**
- Show hints: **ON**; hint source: layout first
- Language switch key: ON (or forced in functional layout)
- Gap scale / keyboard height: tune to match screenshot proportions
- Spacebar shows language name (existing behavior)

**Code defaults (optional, for a “ships looking like the phone” fork):**
- `Defaults.kt`: `PREF_THEME_STYLE = Rounded`, `PREF_THEME_KEY_BORDERS = true`, `PREF_SHOW_NUMBER_ROW = true`, maybe dark color defaults
- Ship a named user color theme, or change dark `keyboard_background_dark` toward pure black
- Toolbar icons in screenshot (sparkle/translate/…) are Gboard/OEM chrome — HeliBoard toolbar is configurable but **will not be pixel-identical** without custom toolbar icon set / ordering

### (e) What cannot be pixel-perfect without deeper UI work
- Exact Gboard/Samsung toolbar (magic wand, translate glyph, orange emoji badge)
- Exact Done blue shade / pressed animations / font metrics
- Exact key gap/height ratios across all phone densities (tunable but not guaranteed)
- Suggestion strip / “Create New Bot” UI above keyboard (host app, not IME)
- Popup long-press panels and glide paths
- Iconography of Shift/Delete (Rounded set is close, not identical)

## 4. Drafts already on branch `custom-gboard-like-layouts`
- `app/src/main/assets/layouts/main/qwerty_phone.json`
- `app/src/main/assets/layouts/main/arabic_phone.json`
- `app/src/main/assets/layouts/functional/functional_keys_phone.json`
- This plan file

**Not done yet:** `method.xml` / `strings.xml` registration, Defaults theme changes, commit, push (no fork).

## 5. Blockers
1. **Fork missing / cannot create:** `waelkateeb32/HeliBoard` 404; MCP `fork_repository` → 403 “Resource not accessible by integration”. User must fork in browser, or grant fork permission, then we push.
2. **`gh` CLI unauthenticated** on the box; git HTTPS to private/user repos needs credentials.
3. **No JDK/Android SDK** in this environment (`java: command not found`) — cannot build/APK-test here; layout drafts are source-only.
4. Functional `!#1|!code/-10001` should be smoke-tested in-app (code name vs numeric).
5. Confirm AR period hint glyph on a real device screenshot zoom.

## Recommended next actions
1. User forks https://github.com/HeliBorg/HeliBoard → `waelkateeb32/HeliBoard` (or re-auth GitHub with fork scope).
2. Register drafts in `method.xml` + strings; optionally flip Defaults for Rounded/borders/number row.
3. Commit on `custom-gboard-like-layouts`, push to fork, build APK (local Android Studio / CI), install, compare to screenshots, tweak gaps/colors/hints.
