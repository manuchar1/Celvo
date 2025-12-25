package com.mtislab.celvo.api.controller

import com.mtislab.celvo.infra.database.entities.UserEntity
import com.mtislab.celvo.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<UserResponse> {
        val userId = UUID.fromString(jwt.subject)
        val user = userService.findById(userId)

        return if (user != null) {
            ResponseEntity.ok(UserResponse.from(user))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = userService.findById(id)
        return if (user != null) {
            ResponseEntity.ok(UserResponse.from(user))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

data class UserResponse(
    val id: UUID,
    val email: String,
    val fullName: String?,
    val createdAt: Instant,

    ) {
    companion object {
        fun from(user: UserEntity) = UserResponse(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            createdAt = user.createdAt,

            )
    }
}
