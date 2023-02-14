package io.supabase.supabasespringbootstarter.application

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
class WebController {
    @GetMapping("/")
    fun index() = "index"

    @GetMapping("/admin")
    fun admin() = "admin"

    @GetMapping("/account")
    fun manager(): String {

        println(SecurityContextHolder.getContext().authentication)
        return "account"
    }

    @GetMapping("/recover-password")
    fun recoverPassword() = "updatePassword"
}
