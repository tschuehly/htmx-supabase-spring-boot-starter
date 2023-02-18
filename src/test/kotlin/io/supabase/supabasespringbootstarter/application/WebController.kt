package io.supabase.supabasespringbootstarter.application

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView


@Controller
class WebController {
    @GetMapping("/")
    fun index() = "index"

    @GetMapping("/admin")
    fun admin() = "admin"

    @GetMapping("/account")
    fun account(): String {
        return "account"
    }

    @GetMapping("/unauthenticated")
    fun unauthenticated(): ModelAndView {
        return ModelAndView("/unauthenticated", HttpStatus.FORBIDDEN)
    }

    @GetMapping("/unauthorized")
    fun unauthorized(): ModelAndView {
        return ModelAndView("/unauthorized", HttpStatus.FORBIDDEN)
    }

    @GetMapping("/updatePassword")
    fun updatePassword(): String {
        return "updatePassword"
    }

    @GetMapping("/requestPasswordReset")
    fun requestPasswordReset(): String {
        return "requestPasswordReset"
    }
}
