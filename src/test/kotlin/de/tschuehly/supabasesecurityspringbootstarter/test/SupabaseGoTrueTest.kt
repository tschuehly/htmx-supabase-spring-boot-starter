package de.tschuehly.supabasesecurityspringbootstarter.test

import de.tschuehly.supabasesecurityspringbootstarter.application.TestApplication
import de.tschuehly.supabasesecurityspringbootstarter.test.mock.GoTrueMock
import de.tschuehly.supabasesecurityspringbootstarter.test.mock.GoTrueMockConfiguration
import org.assertj.core.api.BDDAssertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.*
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import org.springframework.web.util.DefaultUriBuilderFactory

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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SupabaseGoTrueTest {
    lateinit var restClient: RestClient
    @LocalServerPort
    var port: Int? = null
    @BeforeAll
    fun setup() {
        restClient = RestClient.create("http://localhost:$port")
    }

    @Test
    fun `login returns set-cookie`() {
        val response = restClient.post()
            .uri("/api/user/login")
            .form("email" to "email@example.com", "password" to GoTrueMock.VALID_PASSWORD)
            .retrieve().toEntity<String>()
        then(response.statusCode).isEqualTo(HttpStatus.OK)
        then(response.headers["Set-Cookie"]?.get(0)).startsWith("JWT=new_access_token; Max-Age=6000; Expires=Sun, ").endsWith(" GMT; Path=/; HttpOnly")
    }

    private fun RestClient.RequestBodySpec.form(
        vararg formdata: Pair<String, String>, jwt: String? = null
    ): RestClient.RequestBodySpec {
        return this.contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .addJWTCookie(jwt)
            .body(
                LinkedMultiValueMap(formdata.groupBy({ it.first }, { it.second }))
            )
    }

    private fun RestClient.RequestBodySpec.addJWTCookie(jwt: String?): RestClient.RequestBodySpec {
        jwt?.let {
            this.headers {
                it.add("Cookie", "JWT=" + StringUtils.trimAllWhitespace(jwt))
            }
        }
        return this
    }
}