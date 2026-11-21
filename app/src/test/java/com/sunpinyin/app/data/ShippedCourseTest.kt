package com.sunpinyin.app.data

import com.sunpinyin.domain.ExerciseEngine
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** Verifies the JSON actually shipped in assets produces usable content. */
class ShippedCourseTest {
    private val shippedJson by lazy {
        val file = File("src/main/assets/pinyin_course.json")
        check(file.exists()) { "Missing shipped asset: ${file.absolutePath}" }
        file.readText(Charsets.UTF_8)
    }

    @Test
    fun `every lesson has speech text and at least two practice characters`() {
        val course = CourseJsonParser.parse(shippedJson)
        assertTrue(course.lessons.size >= 14, "Expected all 14 textbook lessons")
        course.lessons.forEach { lesson ->
            assertTrue(lesson.pinyinItems.isNotEmpty(), "Lesson ${lesson.id} has no pinyin items")
            assertTrue(
                lesson.pinyinItems.all { it.speechText.isNotBlank() },
                "Lesson ${lesson.id} has an item without speech text"
            )
            assertTrue(
                lesson.exampleCharacters.size >= 2,
                "Lesson ${lesson.id} has fewer than two example characters"
            )
        }
    }

    @Test
    fun `every lesson can generate both listen and character exercises`() {
        val course = CourseJsonParser.parse(shippedJson)
        val engine = ExerciseEngine(course)
        course.lessons.forEach { lesson ->
            assertTrue(
                engine.listenAndChoosePinyin(lesson.id).isNotEmpty(),
                "Lesson ${lesson.id} cannot produce listen exercise"
            )
            assertTrue(
                engine.chooseCharacterForPinyin(lesson.id).isNotEmpty(),
                "Lesson ${lesson.id} cannot produce character exercise"
            )
        }
    }

    @Test
    fun `tone and blend exercises are available`() {
        val course = CourseJsonParser.parse(shippedJson)
        val engine = ExerciseEngine(course)
        assertTrue(engine.toneExercises().isNotEmpty())
        assertTrue(engine.blendExercises().isNotEmpty())
    }
}
