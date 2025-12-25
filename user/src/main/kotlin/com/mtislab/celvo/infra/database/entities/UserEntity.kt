package com.mtislab.celvo.infra.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.Instant
import java.util.UUID

@Entity
@Immutable
@Table(
    name = "users",
    schema = "user_service",
    indexes = [
        Index(name = "idx_users_email", columnList = "email")
    ]
)
class UserEntity(
    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(name = "full_name")
    val fullName: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant
)