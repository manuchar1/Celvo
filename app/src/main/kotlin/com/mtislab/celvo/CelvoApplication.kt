package com.mtislab.celvo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CelvoApplication

fun main(args: Array<String>) {
	runApplication<CelvoApplication>(*args)
}
