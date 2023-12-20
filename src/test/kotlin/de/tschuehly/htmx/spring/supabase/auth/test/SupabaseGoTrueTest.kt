package de.tschuehly.htmx.spring.supabase.auth.test

import com.auth0.jwt.JWTVerifier
import de.tschuehly.htmx.spring.supabase.auth.application.TestApplication
import de.tschuehly.htmx.spring.supabase.auth.test.mock.GoTrueMock
import de.tschuehly.htmx.spring.supabase.auth.test.mock.GoTrueMockConfiguration
import org.assertj.core.api.BDDAssertions.assertThatExceptionOfType
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.client.HttpClientErrorException
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
class SupabaseGoTrueTest {
    lateinit var restClient: RestClient
    lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    var port: Int? = null

    @BeforeEach
    fun setup() {
        restClient = RestClient.builder().baseUrl("http://localhost:$port").build()
        restTemplate = TestRestTemplate()
        restTemplate.setUriTemplateHandler(DefaultUriBuilderFactory("http://localhost:$port"))
    }

    @MockBean
    lateinit var jwtVerifier: JWTVerifier

    @Test
    fun `User should be able to login`() {
        restClient.post().uri("/api/user/login")
            .form("email" to "email@example.com", "password" to GoTrueMock.VALID_PASSWORD)
            .retrieve().toBodilessEntity()
            .let {
                then(it.statusCode).isEqualTo(HttpStatus.OK)
                then(it.headers["Set-Cookie"]?.get(0)).startsWith("JWT=new_access_token; Max-Age=6000; Expires=")
                    .endsWith(" GMT; Path=/; HttpOnly")
            }
    }

    @Test
    fun `User should be able to signup with Email`() {
        restClient.post()
            .uri("/api/user/signup")
            .form("email" to "email@example.com", "password" to GoTrueMock.VALID_PASSWORD)
            .asString()
            .let {
                then(it.statusCode).isEqualTo(HttpStatus.OK)
                then(it.body).isEqualTo("SuccessfulRegistrationConfirmationEmailSent")
            }
    }

    @Test
    fun `Unauthorized User cannot access account site`() {
        assertThatExceptionOfType(HttpClientErrorException::class.java).isThrownBy {
            restClient.get().uri("/account").retrieve().toBodilessEntity()
        }.withMessage("403 : \"You need to sign in to access this side.\"")

    }

    @Test
    fun `Unauthorized User will be redirect to unauthenticated`() {
        restTemplate.getForEntity<String>("/account").let {
            then(it.statusCode).isEqualTo(HttpStatus.FOUND)
            then(it.headers["Location"]?.get(0)).endsWith("/unauthenticated")

        }
    }

    @Test
    fun `User will be redirect to unauthenticated`() {
        restTemplate.getForEntity<String>("/account").let {
            then(it.statusCode).isEqualTo(HttpStatus.FOUND)
            then(it.headers["Location"]?.get(0)).endsWith("/unauthenticated")

        }
    }

    @Test
    fun `Unauthorized User can access public site`() {
        restTemplate.getForEntity<String>("/").let {
            then(it.statusCode).isEqualTo(HttpStatus.OK)
        }
    }

    @Test
    @Disabled
    fun `User can login and access the account page`() {
//        TODO: how to mock
        Mockito.`when`(jwtVerifier.verify("new_access_token").claims).thenReturn(mapOf())
        restClient.post().uri("/api/user/login")
            .form("email" to "email@example.com", "password" to GoTrueMock.VALID_PASSWORD)
            .retrieve().toBodilessEntity()
        restClient.get().uri("/account").retrieve().toBodilessEntity()
    }

    private fun RestClient.RequestBodySpec.asString(): ResponseEntity<String> {
        return this.retrieve().toEntity<String>()
    }

    private fun RestClient.RequestBodySpec.form(
        vararg formdata: Pair<String, String>, jwt: String? = null
    ): RestClient.RequestBodySpec {
        return this.contentType(MediaType.APPLICATION_FORM_URLENCODED).addJWTCookie(jwt).body(
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