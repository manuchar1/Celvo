package com.mtislab.celvo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

@RestController
@RequestMapping("/api")
class TestController {


    @GetMapping("/private/hello")
    fun helloPrivate(@AuthenticationPrincipal jwt: Jwt): String {
        val userId = jwt.subject // Supabase-ის მომხმარებლის ID (UUID)
        return "გამარჯობა! შენ წარმატებით გაიარე ავტორიზაცია. შენი ID არის: $userId"
    }

    @GetMapping("/public/hello")
    fun helloPublic(): String {
        return "ეს საჯარო ინფორმაციაა და ტოკენი არ სჭირდება."
    }
}