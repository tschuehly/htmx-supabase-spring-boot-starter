package io.supabase.supabasespringbootstarter.controller

import io.supabase.supabasespringbootstarter.exception.UserNeedsToConfirmEmail
import io.supabase.supabasespringbootstarter.service.SupabaseUserService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
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
            val user = supabaseUserService.registerWithEmail(email, password, response)
            throw UserNeedsToConfirmEmail("User with the mail ${user.email} needs to confirm signup")
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
    fun authorizeWithJwtOrResetPassword(request: HttpServletRequest, response: HttpServletResponse) {
        supabaseUserService.authorizeWithJwtOrResetPassword(request, response)
    }

    @GetMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        supabaseUserService.logout(request, response)
    }

    @PutMapping("/setRoles")
    @ResponseBody
    fun setRoles(
        @RequestParam
        roles: List<String>?,
        request: HttpServletRequest,
        @RequestParam
        userId: String,
    ) {
        if (userId == "") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "UserId required")
        }
        supabaseUserService.setRoles(userId, request, roles)
    }

    @PostMapping("/sendPasswordResetEmail")
    @ResponseBody
    fun sendPasswordResetEmail(
        @RequestParam
        email: String
    ) {
        supabaseUserService.sendPasswordResetEmail(email)
    }

    @PostMapping("/updatePassword")
    @ResponseBody
    fun updatePassword(
        request: HttpServletRequest,
        @RequestParam
        password: String
    ) {
        supabaseUserService.updatePassword(request, password)
    }
}
