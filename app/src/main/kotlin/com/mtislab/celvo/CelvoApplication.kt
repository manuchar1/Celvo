package com.mtislab.celvo

import com.mtislab.celvo.security.SupabaseProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication(scanBasePackages = ["com.mtislab", "config"])
@EnableJpaRepositories(basePackages = ["com.mtislab"])
@EntityScan(basePackages = ["com.mtislab"])
@EnableConfigurationProperties(SupabaseProperties::class)
@EnableScheduling
@EnableAsync
class CelvoApplication

fun main(args: Array<String>) {
    runApplication<CelvoApplication>(*args)
}


