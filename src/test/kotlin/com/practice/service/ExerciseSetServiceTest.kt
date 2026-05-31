package com.practice.service

import com.practice.domain.ExerciseSet
import com.practice.domain.ExerciseType
import com.practice.dto.ExerciseSetCreateRequest
import com.practice.repository.ExerciseSetRepository
import com.practice.repository.TeacherRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID

class ExerciseSetServiceTest {

    private val exerciseSetRepository = mock(ExerciseSetRepository::class.java)
    private val teacherRepository = mock(TeacherRepository::class.java)
    private val exerciseSetService = ExerciseSetService(exerciseSetRepository, teacherRepository)

    @Test
    fun `test createExerciseSet generates slug from title`() {
        val request = ExerciseSetCreateRequest(
            title = "Spanish Preterite",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "Yo [hablé] español."
        )

        setupMockSave()
        `when`(exerciseSetRepository.findByShareSlug(anyString())).thenReturn(null)

        val response = exerciseSetService.createExerciseSet(null, request)

        assertThat(response.shareSlug).startsWith("spanish-preterite-")
        assertThat(response.shareSlug?.length).isEqualTo("spanish-preterite-".length + 6)
    }

    @Test
    fun `test generateUniqueShareSlug handles long titles and trailing hyphens`() {
        val request = ExerciseSetCreateRequest(
            title = "A".repeat(49) + "-something",
            type = ExerciseType.FILL_GAP_TEXT,
            bulkInput = "Yo [hablé] español."
        )

        setupMockSave()
        `when`(exerciseSetRepository.findByShareSlug(anyString())).thenReturn(null)

        val response = exerciseSetService.createExerciseSet(null, request)
        
        assertThat(response.shareSlug).startsWith("a".repeat(49) + "-")
        assertThat(response.shareSlug).doesNotContain("--")
    }

    private fun setupMockSave() {
        `when`(exerciseSetRepository.save(any())).thenAnswer {
            val set = it.arguments[0] as ExerciseSet
            if (set.id == null) {
                set.id = UUID.randomUUID()
            }
            set
        }
    }
}
