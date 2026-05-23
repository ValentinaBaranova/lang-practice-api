package com.practice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class TeacherAccessTest : IntegrationTestBase() {

    @Test
    fun `fetching exercises with invalid accessCode should return 404`() {
        val response = restTemplate.getForEntity(url("/api/exercise-sets?accessCode=INVALID_CODE"), Map::class.java)
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        val body = response.body as Map<*, *>
        assertThat(body["message"]?.toString()).contains("Teacher not found with accessCode: INVALID_CODE")
    }

    @Test
    fun `fetching teacher info with invalid accessCode should return 404`() {
        val response = restTemplate.getForEntity(url("/api/teachers/INVALID_CODE"), Map::class.java)
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        val body = response.body as Map<*, *>
        assertThat(body["message"]?.toString()).contains("Teacher not found with accessCode: INVALID_CODE")
    }
}
