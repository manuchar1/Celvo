package com.mtislab.celvo

import com.mtislab.celvo.infra.database.entities.UserEntity
import com.mtislab.celvo.repository.UserRepository
import com.mtislab.celvo.security.SupabaseProperties
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID



@SpringBootApplication
@EnableJpaRepositories(basePackages = ["com.mtislab"])
@EntityScan(basePackages = ["com.mtislab"])
@EnableConfigurationProperties(SupabaseProperties::class)
class CelvoApplication

fun main(args: Array<String>) {
    runApplication<CelvoApplication>(*args)
}

@Component
class Demo(private val userRepository: UserRepository) {

    @PostConstruct
    fun init() {
        val email = "test2@manuchar.com"
        if (!userRepository.existsByEmail(email)) {
            try {
                userRepository.save(
                    UserEntity(
                        id = UUID.randomUUID(),
                        email = email,
                        fullName = "manuchar",
                        createdAt = Instant.now()
                    )
                )
            } catch (e: Exception) {
                println("Failed to seed demo user: ${e.message}")
            }
        }
    }
}
