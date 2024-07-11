package de.tschuehly.htmx.spring.supabase.auth.htmx

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponseHeader
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxSwapType
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.UriTemplate

object HtmxUtil {
    fun idSelector(id: String): String {
        return "#$id"
    }

    fun retarget(cssSelector: String?) {
        setHeader(HtmxResponseHeader.HX_RETARGET.getValue(), cssSelector)
    }

    fun swap(hxSwapType: HxSwapType) {
        setHeader(HtmxResponseHeader.HX_RESWAP.getValue(), hxSwapType.getValue())
    }


    fun trigger(event: String?) {
        setHeader(HtmxResponseHeader.HX_TRIGGER.getValue(), event)
    }

    fun URI(uriTemplate: String?, vararg variables: Any?): String {
        return UriTemplate(uriTemplate!!).expand(*variables).toString()
    }

    fun setHeader(headerName: String?, headerValue: String?) {
            getResponse().setHeader(headerName, headerValue)
    }

    fun getResponse(): HttpServletResponse {
        return (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.response
            ?: throw RuntimeException("No response found in RequestContextHolder")
    }

    fun getRequest(): HttpServletRequest {
        return (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
            ?: throw RuntimeException("No response found in RequestContextHolder")
    }



}