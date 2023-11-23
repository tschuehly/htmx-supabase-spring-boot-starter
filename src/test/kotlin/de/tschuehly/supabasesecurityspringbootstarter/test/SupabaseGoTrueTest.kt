package de.tschuehly.supabasesecurityspringbootstarter.test

import de.tschuehly.supabasesecurityspringbootstarter.application.TestApplication
import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.*
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["debug=org.springframework.security"],
)
@TestPropertySource(
    properties = ["SUPABASE_PROJECT_ID=", "SUPABASE_ANON_KEY=", "SUPABASE_DATABASE_PW=", "SUPABASE_JWT_SECRET="]
)
@Import(GoTrueMockConfiguration::class)
class SupabaseGoTrueTest {
    val restTemplate = TestRestTemplate()
    @LocalServerPort
    var port: Int? = null
    @Test
    fun test(){
        val loginResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/api/user/login", HttpMethod.POST, getFormDataEntity(
                formdata = arrayOf("email" to "email@example.com", "password" to GoTrueMock.VALID_PASSWORD), jwt = null
            ), String::class.java
        )
        BDDAssertions.then(loginResponse.statusCode).isEqualTo(HttpStatus.OK)
        BDDAssertions.then(loginResponse.headers["Set-Cookie"]!![0]).isNotNull
    }

    private fun getFormDataEntity(
        vararg formdata: Pair<String, String>, jwt: String?
    ): HttpEntity<MultiValueMap<String, String>> {
        val headers = jwt?.let {
            getHeaderForJwt(jwt)
        } ?: HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val map = LinkedMultiValueMap<String, String>()
        formdata.forEach {
            map.add(it.first, it.second)
        }
        return HttpEntity(map, headers)
    }

    private fun getHeaderForJwt(jwt: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("Cookie", "JWT=" + StringUtils.trimAllWhitespace(jwt))
        return headers
    }
}