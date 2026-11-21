package com.sunpinyin.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CourseJsonParserTest {
    private val validJson = """
        {
          "textbookVersion": "2024",
          "toneDemos": [
            {"toneNumber": 1, "toneName": "一声", "mark": "ˉ", "exampleCharacter": "妈", "exampleSyllable": "mā"},
            {"toneNumber": 2, "toneName": "二声", "mark": "ˊ", "exampleCharacter": "麻", "exampleSyllable": "má"}
          ],
          "blendDemos": [
            {"initial": "b", "final": "a", "syllable": "ba", "exampleCharacter": "爸"},
            {"initial": "m", "final": "a", "syllable": "ma", "exampleCharacter": "妈"}
          ],
          "units": [{
            "id": "u1",
            "title": "unit",
            "lessons": [{
              "id": "l1",
              "title": "b p m f",
              "items": [
                {"id": "b", "label": "b", "kind": "INITIAL", "speechText": "玻"},
                {"id": "m", "label": "m", "kind": "INITIAL", "speechText": "摸"}
              ],
              "characters": [{"character": "爸", "syllable": "bà", "itemId": "b"}]
            }]
          }]
        }
    """.trimIndent()

    @Test
    fun `valid json parses into course with speech text`() {
        val course = CourseJsonParser.parse(validJson)
        assertEquals("2024", course.textbookVersion)
        assertEquals("玻", course.lessons.first().pinyinItems.first().speechText)
        assertEquals("爸", course.lessons.first().exampleCharacters.first().character)
        assertEquals(2, course.toneDemos.size)
        assertEquals(2, course.blendDemos.size)
    }

    @Test
    fun `missing speech text fails at repository seam`() {
        val broken = validJson.replace("\"speechText\": \"玻\"", "\"speechText\": \"\"")
        assertFailsWith<IllegalArgumentException> { CourseJsonParser.parse(broken) }
    }

    @Test
    fun `character without matching pinyin item fails at repository seam`() {
        val broken = validJson.replace("\"itemId\": \"b\"", "\"itemId\": \"x\"")
        assertFailsWith<IllegalArgumentException> { CourseJsonParser.parse(broken) }
    }
}
