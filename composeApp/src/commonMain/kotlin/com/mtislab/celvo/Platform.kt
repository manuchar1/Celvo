package com.mtislab.celvo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform