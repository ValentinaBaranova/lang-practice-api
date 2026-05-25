package com.practice

import com.practice.dto.TeacherCreateRequest
import com.practice.dto.TeacherResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class TeacherIntegrationTest : IntegrationTestBase() {

    @Test
    fun `should create a new teacher with provided name`() {
        val request = TeacherCreateRequest(name = "New Teacher")
        val response = restTemplate.postForEntity(url("/api/teachers"), request, TeacherResponse::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.name).isEqualTo("New Teacher")
    }

    @Test
    fun `should return 400 when teacher name is blank`() {
        val request = TeacherCreateRequest(name = "")
        val response = restTemplate.postForEntity(url("/api/teachers"), request, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should create teacher on the fly when authenticated with Google`() {
        val headers = authHeaders()
        val entity = org.springframework.http.HttpEntity<Unit>(headers)
        
        val response = restTemplate.exchange(
            url("/api/teachers/me"),
            org.springframework.http.HttpMethod.GET,
            entity,
            TeacherResponse::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.email).isEqualTo("test@example.com")
        assertThat(response.body?.name).isEqualTo("Test Teacher")
    }
}
