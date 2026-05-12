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
        assertThat(response.body?.accessCode).isNotNull().hasSize(10)
    }

    @Test
    fun `should return 400 when teacher name is blank`() {
        val request = TeacherCreateRequest(name = "")
        val response = restTemplate.postForEntity(url("/api/teachers"), request, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}
