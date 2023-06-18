package de.tschuehly.supabasesecurityspringbootstarter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.types.GoTrueTokenResponse
import de.tschuehly.supabasesecurityspringbootstarter.application.TestApplication
import de.tschuehly.supabasesecurityspringbootstarter.types.SupabaseUser
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class SupabaseIntegrationTest() {

    @Autowired
    lateinit var supabaseGoTrueClient: GoTrueClient<SupabaseUser, GoTrueTokenResponse>

    @LocalServerPort
    var port: Int? = null

    private var wireMockServer: WireMockServer = WireMockServer(9999)

    val restTemplate = TestRestTemplate()

    @BeforeEach
    fun proxyToWireMock() {
        wireMockServer.start()
        supabaseGoTrueClient = GoTrueClient.customApacheJacksonGoTrueClient(
            url = "http://localhost:${wireMockServer.port()}",
            headers = emptyMap()
        )
    }

    @AfterEach
    fun noMoreWireMock() {
        wireMockServer.stop()
        wireMockServer.resetAll()
    }


    @Test
    fun `User should be able to register with Email`() {
        wireMockServer.stubFor(
            post("/signup")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(fixture("/fixtures/signup-response.json"))
                )
        )
        val registerEntity: ResponseEntity<String> = restTemplate.postForEntity(
            "http://localhost:$port/api/user/register", HttpEntity(null, null), String::class.java
        )
        then(registerEntity.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Unauthorized User should not be able to access the account page`() {
        val accountResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/account", HttpMethod.GET, HttpEntity(null, null), String::class.java
        )

        then(accountResponse.statusCode)
            .isEqualTo(HttpStatus.FOUND)
        then(accountResponse.headers.get("Location")!![0])
            .endsWith("/unauthenticated")
    }

    @Test
    fun `Unauthorized User can access public sites`() {
        val indexResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port", HttpMethod.GET, HttpEntity(null, null), String::class.java
        )
        then(indexResponse.statusCode).isEqualTo(HttpStatus.OK)
    }


    @Test
    fun `Authenticated User cannot set Roles for Users`() {
        wireMockServer.stubFor( //TODO: Is it even useful to test it this way?
            put("/admin/users/54a12619-cee3-4856-a854-bad14d4639ed")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(403)
                )
        )
        val userResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/api/user/setRoles",
            HttpMethod.PUT,
            getFormDataEntity(
                formdata = arrayOf("userId" to "54a12619-cee3-4856-a854-bad14d4639ed", "roles" to "user"),
                jwt = fixture("/fixtures/valid-user-jwt.txt")
            ),
            String::class.java
        )

        then(userResponse.statusCode)
            .isEqualTo(HttpStatus.FOUND)
        then(userResponse.headers.get("Location")!![0])
            .endsWith("/unauthenticated")

    }

    @Test
    fun `Service Role User can set Roles for Users`() {
        wireMockServer.stubFor(
            put("/admin/users/54a12619-cee3-4856-a854-bad14d4639ed")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(fixture("/fixtures/signup-response.json"))
                )
        )
        val userResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/api/user/setRoles",
            HttpMethod.PUT,
            getFormDataEntity(
                formdata = arrayOf("userId" to "54a12619-cee3-4856-a854-bad14d4639ed", "roles" to "user"),
                jwt = createJWT()
            ),
            String::class.java
        )
        then(userResponse.statusCode).isEqualTo(HttpStatus.OK)

    }

    @Test
    fun `User can login and access the account page`() {
        wireMockServer.stubFor(
            post("/token?grant_type=password")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(fixture("/fixtures/login-response.json"))
                )
        )
        val loginResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/api/user/login",
            HttpMethod.POST,
            getFormDataEntity(
                formdata = arrayOf("email" to "first.last@example.com", "password" to "test1234"),
                jwt = null
            ),
            String::class.java
        )
        then(loginResponse.statusCode).isEqualTo(HttpStatus.OK)
        then(loginResponse.headers["Set-Cookie"]!![0]).isNotNull

        val headers = HttpHeaders()
        headers.add("Cookie", loginResponse.headers["Set-Cookie"]!![0].substringBefore(";"))
        val accountResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/account", HttpMethod.GET, HttpEntity<String>(headers), String::class.java
        )

        then(accountResponse.statusCode).isEqualTo(HttpStatus.OK)
        then(
            accountResponse.body!!.contains("<h1>You are authenticated</h1>")
        )
    }

    @Test
    fun `User with expired JWT cannot access the account page`() {
        val accountResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/account",
            HttpMethod.GET,
            HttpEntity<String>(getHeaderForJwt(fixture("/fixtures/expired-user-jwt.txt"))),
            String::class.java
        )

        then(accountResponse.statusCode)
            .isEqualTo(HttpStatus.FOUND)
        then(accountResponse.headers.get("Location")!![0])
            .endsWith("/unauthenticated")
    }

    @Test
    fun `Normal user can't access admin page and gets redirected to unauthenticated`() {
        val adminResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/admin", HttpMethod.GET, HttpEntity(null, null), String::class.java
        )

        then(adminResponse.statusCode)
            .isEqualTo(HttpStatus.FOUND)
        then(adminResponse.headers["Location"]!![0])
            .endsWith("/unauthenticated")
    }

    @Test
    fun `Normal User cannot access Admin Page`(){
        val adminResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/admin",
            HttpMethod.GET,
            HttpEntity<String>(getHeaderForJwt(fixture("/fixtures/valid-user-jwt.txt"))),
            String::class.java
        )
        then(adminResponse.statusCode)
            .isEqualTo(HttpStatus.FOUND)
        then(adminResponse.headers["Location"]!![0])
            .endsWith("/unauthorized")
    }

    @Test
    fun `User with Admin BasicAuth can access Admin Page`(){
        val basicAuthHeader = HttpHeaders()
        basicAuthHeader.add("Authorization","Basic YWRtaW46cGFzc3dvcmQ=")
        val adminResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/admin",
            HttpMethod.GET,
            HttpEntity(null,basicAuthHeader),
            String::class.java
        )
        then(adminResponse.statusCode)
            .isEqualTo(HttpStatus.OK)
    }

    private fun getFormDataEntity(
        vararg formdata: Pair<String, String>,
        jwt: String?
    ): HttpEntity<MultiValueMap<String, String>> {
        val headers = jwt?.let {
            getHeaderForJwt(jwt)
        } ?: HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED;
        val map = LinkedMultiValueMap<String, String>();
        formdata.forEach {
            map.add(it.first, it.second)
        }
        return HttpEntity(map, headers);
    }


    private fun fixture(path: String): String {
        return SupabaseIntegrationTest::class.java.getResource(path)?.readText()
            ?: throw Exception("Fixture file not found")
    }

    private fun getHeaderForJwt(jwt: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("Cookie", "JWT=" + StringUtils.trimAllWhitespace(jwt))
        return headers
    }
}


fun createJWT(): String {
    return JWT.create()
        .withPayload(
            mapOf(
                "aud" to "authenticated",
                "sub" to "f802c3bb-223e-43a6-bba0-5ae6094f0d91",
                "email" to "first.last@example.com",
                "app_metadata" to
                        """{
                  "provider": "email",
                  "providers": [
                    "email"
                  ]
                }""",
                "role" to "service_role"
            )
        )
        .sign(Algorithm.HMAC256("VhLI85yN/oF3Eu95epgHOeg/iRIGiJtk2PWyCyCdORRuVVW90wToyJcJXZcHuHZ2dh7qVgH0UMjqbq1gGMF6JQ=="))
}
