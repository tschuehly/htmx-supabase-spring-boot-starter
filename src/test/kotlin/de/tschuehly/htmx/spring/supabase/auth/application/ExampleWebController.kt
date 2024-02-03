package de.tschuehly.htmx.spring.supabase.auth.application

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView
import javax.sql.DataSource


@Controller
class ExampleWebController(
) {
    @Autowired
    var dataSource: DataSource? = null
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

    @GetMapping("/jdbc")
    @ResponseBody
    fun jdbc(): String {
        return dataSource?.let {
             JdbcTemplate(it).queryForObject("Select Count(*) from auth.users ", String::class.java)
        } ?: "No dataSource"
    }
}
