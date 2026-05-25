package com.practice.config

import com.practice.service.GoogleTokenService
import com.practice.service.TeacherService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val googleTokenService: GoogleTokenService,
    private val teacherService: TeacherService
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/exercises/**").permitAll() // Public exercises
                    .requestMatchers("/api/attempts/**").permitAll() // Students can submit attempts
                    .requestMatchers(HttpMethod.GET, "/api/exercise-sets/share/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/exercise-sets/public").permitAll()
                    .requestMatchers("/api/teachers/me").authenticated()
                    .requestMatchers("/api/ai/**").authenticated()
                    .requestMatchers("/api/exercise-sets/**").authenticated()
                    .anyRequest().permitAll()
            }
            .addFilterBefore(TokenAuthenticationFilter(googleTokenService, teacherService), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}

class TokenAuthenticationFilter(
    private val googleTokenService: GoogleTokenService,
    private val teacherService: TeacherService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            val googleUserInfo = googleTokenService.verify(token)
            if (googleUserInfo != null) {
                val teacher = teacherService.getOrCreateTeacher(googleUserInfo.email, googleUserInfo.name)
                val authentication = UsernamePasswordAuthenticationToken(
                    teacher,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_TEACHER"))
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        }
        filterChain.doFilter(request, response)
    }
}
