package io.supabase.supabasespringbootstarter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.post
import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.types.GoTrueTokenResponse
import io.supabase.supabasespringbootstarter.types.SupabaseUser
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
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "supabase.projectId=askljdaslkjdsa",
        "supabase.anonKey=asljkhdas",
        "supabase.databasePassword=kaskjsad",
        "supabase.jwtSecret=VhLI85yN/oF3Eu95epgHOeg/iRIGiJtk2PWyCyCdORRuVVW90wToyJcJXZcHuHZ2dh7qVgH0UMjqbq1gGMF6JQ==",
        "supabase.public.get[0]=/",
        "supabase.public.get[1]=/logout",
        "supabase.public.get[2]=/login",
        "supabase.public.get[3]=/error",
        "supabase.public.post[0]=/api/user/register",
        "supabase.public.post[1]=/api/user/login",
        "supabase.public.post[2]=/api/user/jwt",
        "supabase.roles.admin.get[0]=/", //TODO: User based Authoriization
        "debug=org.springframework.security"],
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
        then(accountResponse.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Unauthorized User can access public sites`(){
        val indexResponse : ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port", HttpMethod.GET, HttpEntity(null, null), String::class.java
        )
        then(indexResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Normal user cannot access admin page`() {
        val adminResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/admin", HttpMethod.GET, HttpEntity(null, null), String::class.java
        )
        then(adminResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Can add Role to User`() {
        val userResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/api/user/addRole", HttpMethod.GET, HttpEntity(null, null), String::class.java

        )
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
            "http://localhost:$port/api/user/login", HttpMethod.POST, getLoginRequest(), String::class.java
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
            StringUtils.trimAllWhitespace(accountResponse.body!!)
        )
            .isEqualTo("""<!DOCTYPEhtml><htmlxmlns="http://www.w3.org/1999/xhtml"lang="de"><head><metacharset="UTF-8"><title>Title</title></head>Loggeduser:<span>f802c3bb-223e-43a6-bba0-5ae6094f0d91</span><body><h1>Youareauthenticated</h1></body></html>""")
    }

    @Test
    fun `User with expired JWT cannot access the account page`() {
        val headers = HttpHeaders()
        headers.add("Cookie", "JWT=" + StringUtils.trimAllWhitespace(fixture("/fixtures/expired-jwt.txt")))
        val accountResponse: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/account", HttpMethod.GET, HttpEntity<String>(headers), String::class.java
        )
        then(accountResponse.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    private fun getLoginRequest(): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED;
        val map = LinkedMultiValueMap<String, String>();
        map.add("email", "first.last@example.com");
        map.add("password", "test1234");
        return HttpEntity(map, headers);
    }

    private fun fixture(path: String): String {
        return SupabaseIntegrationTest::class.java.getResource(path)?.readText()
            ?: throw Exception("Fixture file not found")
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
                }"""
            )
        )
        .sign(Algorithm.HMAC256("VhLI85yN/oF3Eu95epgHOeg/iRIGiJtk2PWyCyCdORRuVVW90wToyJcJXZcHuHZ2dh7qVgH0UMjqbq1gGMF6JQ=="))
}
