package io.supabase.supabasespringbootstarter.controller

import io.supabase.supabasespringbootstarter.service.SupabaseUserService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("api/user")
class SupabaseUserController(
    val supabaseUserService: SupabaseUserService
) {
    @PostMapping("/register")
    fun register(
        @RequestParam credentials: Map<String, String>,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val email = credentials["email"]
        val password = credentials["password"]
        if (email != null && password != null) {
            supabaseUserService.registerWithEmail(email, password, response)
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestParam credentials: Map<String, String>,
        response: HttpServletResponse,
        request: HttpServletRequest
    ) {
        val email = credentials["email"]
        val password = credentials["password"]
        if (email != null && password != null) {
            supabaseUserService.login(email, password, response)
        }
    }

    @PostMapping("/jwt")
    fun authorizeWithJWT(request: HttpServletRequest, response: HttpServletResponse) {
        supabaseUserService.authorizeWithJWT(request, response)
    }

    @GetMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        supabaseUserService.logout(request, response)
    }


}
