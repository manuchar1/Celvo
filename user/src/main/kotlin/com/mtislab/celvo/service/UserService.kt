package com.mtislab.celvo.service

import com.mtislab.celvo.infra.database.entities.UserEntity
import com.mtislab.celvo.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * Find a user by ID (UUID from Supabase Auth).
     * Users are automatically created via database trigger when they sign up.
     */
    fun findById(userId: UUID): UserEntity? {
        return userRepository.findById(userId).orElse(null)
    }

    /**
     * Find a user by email.
     */
    fun findByEmail(email: String): UserEntity? {
        return userRepository.findByEmail(email).orElse(null)
    }

    /**
     * Check if a user exists by email.
     */
    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }
}
