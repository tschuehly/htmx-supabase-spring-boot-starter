package io.supabase.supabasespringbootstarter.security

import com.auth0.jwt.exceptions.TokenExpiredException
import io.supabase.supabasespringbootstarter.config.SupabaseProperties
import io.supabase.supabasespringbootstarter.service.SupabaseUserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SupabaseJwtFilter(
    private val supabaseUserService: SupabaseUserService,
    private val supabaseProperties: SupabaseProperties

) : HttpFilter() {
    val publicPath = antPathsMatcher(
        *supabaseProperties.public.get,
        *supabaseProperties.public.post,
        *supabaseProperties.public.put,
        *supabaseProperties.public.delete,
    )
    val springSecurityPaths = antPathsMatcher("/error")
    val jwtLogger: Logger = LoggerFactory.getLogger(SupabaseJwtFilter::class.java)

    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val jwtCookie = request.cookies?.find { it.name == "JWT" }
        if ((publicPath.matches(request) && jwtCookie == null) || springSecurityPaths.matches(request)) {
            chain.doFilter(request, response)
            return
        }
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication.principal == "anonymousUser" && jwtCookie != null) {
            try {
                supabaseUserService.setAuthentication(jwtCookie.value)
            } catch (e: TokenExpiredException) {
                val oldCookie = request.cookies.find { it.name == "JWT" }
                oldCookie?.maxAge = 0
            }
        }


        if (authentication is AnonymousAuthenticationToken) {
            jwtLogger.info("Request: ${request.requestURI} was blocked")
        }
        chain.doFilter(request, response)
    }

    fun Set<AntPathRequestMatcher>.matches(request: HttpServletRequest): Boolean {
        this.forEach {
            if (it.matches(request)) {
                return true
            }
        }
        return false
    }


    private final fun antPathsMatcher(vararg antPaths: String): Set<AntPathRequestMatcher> {
        return antPaths.map {
            AntPathRequestMatcher(it)
        }.toSet()
    }
}
