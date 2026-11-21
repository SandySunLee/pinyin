package com.sunpinyin.app.data

import android.content.Context
import com.sunpinyin.domain.BlendDemo
import com.sunpinyin.domain.Course
import com.sunpinyin.domain.CourseRepository
import com.sunpinyin.domain.CourseValidator
import com.sunpinyin.domain.ExampleCharacter
import com.sunpinyin.domain.Lesson
import com.sunpinyin.domain.PinyinItem
import com.sunpinyin.domain.PinyinKind
import com.sunpinyin.domain.StudyUnit
import com.sunpinyin.domain.ToneDemo
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
internal data class CourseDto(
    val textbookVersion: String,
    val units: List<UnitDto>,
    val toneDemos: List<ToneDto> = emptyList(),
    val blendDemos: List<BlendDto> = emptyList()
)

@Serializable
internal data class UnitDto(
    val id: String,
    val title: String,
    val lessons: List<LessonDto>
)

@Serializable
internal data class LessonDto(
    val id: String,
    val title: String,
    val items: List<ItemDto>,
    val characters: List<CharacterDto> = emptyList()
)

@Serializable
internal data class ItemDto(
    val id: String,
    val label: String,
    val kind: String,
    val speechText: String
)

@Serializable
internal data class CharacterDto(
    val character: String,
    val syllable: String,
    val itemId: String,
    val speechText: String? = null
)

@Serializable
internal data class ToneDto(
    val toneNumber: Int,
    val toneName: String,
    val mark: String,
    val exampleCharacter: String,
    val exampleSyllable: String
)

@Serializable
internal data class BlendDto(
    val initial: String,
    val final: String,
    val syllable: String,
    val exampleCharacter: String
)

/** Pure JSON-to-course seam; validates content before the domain sees it. */
object CourseJsonParser {
    fun parse(json: String): Course {
        val dto = Json.decodeFromString<CourseDto>(json)
        val course = Course(
            textbookVersion = dto.textbookVersion,
            units = dto.units.map { unit ->
                StudyUnit(
                    id = unit.id,
                    title = unit.title,
                    lessons = unit.lessons.map { lesson ->
                        Lesson(
                            id = lesson.id,
                            title = lesson.title,
                            pinyinItems = lesson.items.map { item ->
                                PinyinItem(
                                    id = item.id,
                                    label = item.label,
                                    kind = PinyinKind.valueOf(item.kind),
                                    speechText = item.speechText
                                )
                            },
                            exampleCharacters = lesson.characters.map { character ->
                                ExampleCharacter(
                                    character = character.character,
                                    syllable = character.syllable,
                                    itemId = character.itemId,
                                    speechText = character.speechText ?: character.character
                                )
                            }
                        )
                    }
                )
            },
            toneDemos = dto.toneDemos.map {
                ToneDemo(
                    it.toneNumber,
                    it.toneName,
                    it.mark,
                    it.exampleCharacter,
                    it.exampleSyllable
                )
            },
            blendDemos = dto.blendDemos.map {
                BlendDemo(it.initial, it.final, it.syllable, it.exampleCharacter)
            }
        )
        CourseValidator.validate(course)
        return course
    }
}

class JsonCourseRepository(private val context: Context) : CourseRepository {
    override fun loadCourse(): Course {
        val json = context.assets.open("pinyin_course.json")
            .bufferedReader().use { it.readText() }
        return CourseJsonParser.parse(json)
    }
}
