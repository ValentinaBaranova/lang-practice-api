package com.practice.service

import com.practice.domain.Teacher
import com.practice.repository.TeacherRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.times

class TeacherServiceTest {

    private val teacherRepository = mock(TeacherRepository::class.java)
    private val teacherService = TeacherService(teacherRepository)

    @Test
    fun `getOrCreateTeacher should return existing teacher if found by email`() {
        val email = "test@example.com"
        val name = "Test User"
        val existingTeacher = Teacher(email = email, name = name)
        
        `when`(teacherRepository.findByEmail(email)).thenReturn(existingTeacher)
        
        val result = teacherService.getOrCreateTeacher(email, name)
        
        assertThat(result).isEqualTo(existingTeacher)
        verify(teacherRepository, times(0)).save(any())
    }

    @Test
    fun `getOrCreateTeacher should update name if it changed`() {
        val email = "test@example.com"
        val newName = "New Name"
        val existingTeacher = Teacher(email = email, name = "Old Name")
        
        `when`(teacherRepository.findByEmail(email)).thenReturn(existingTeacher)
        `when`(teacherRepository.save(any())).thenAnswer { it.arguments[0] as Teacher }
        
        val result = teacherService.getOrCreateTeacher(email, newName)
        
        assertThat(result.name).isEqualTo(newName)
        verify(teacherRepository).save(existingTeacher)
    }

    @Test
    fun `getOrCreateTeacher should create new teacher if not found by email`() {
        val email = "new@example.com"
        val name = "New User"
        
        `when`(teacherRepository.findByEmail(email)).thenReturn(null)
        `when`(teacherRepository.save(any())).thenAnswer { it.arguments[0] as Teacher }
        
        val result = teacherService.getOrCreateTeacher(email, name)
        
        assertThat(result.email).isEqualTo(email)
        assertThat(result.name).isEqualTo(name)
        verify(teacherRepository).save(any())
    }
}
