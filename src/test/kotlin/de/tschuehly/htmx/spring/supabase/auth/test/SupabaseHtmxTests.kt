package de.tschuehly.htmx.spring.supabase.auth.test

import de.tschuehly.htmx.spring.supabase.auth.application.TestApplication
import de.tschuehly.htmx.spring.supabase.auth.test.mock.GoTrueMockConfiguration
import org.assertj.core.api.BDDAssertions.then
import org.htmlunit.SilentCssErrorHandler
import org.htmlunit.WebClient
import org.htmlunit.html.*
import org.htmlunit.javascript.SilentJavaScriptErrorListener
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.context.WebApplicationContext

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
class SupabaseHtmxTests {

    @LocalServerPort
    var port: Int? = null
    val webClient: WebClient = WebClient()

    @BeforeEach
    fun setup(context: WebApplicationContext?) {
        webClient.options.isThrowExceptionOnScriptError = false;
        webClient.setCssErrorHandler(SilentCssErrorHandler())
        webClient.javaScriptErrorListener = SilentJavaScriptErrorListener()
    }

    @Test
    fun invalidLoginCredentialsThrowExceptionWhenLogin() {
        val page: HtmlPage = webClient.getPage("http://localhost:$port/")
        val form = page.getFormByName("login-form")
        val emailInput: HtmlTextInput = form.getInputByName("email")
        emailInput.type("mail@example.com")
        val passwordInput: HtmlPasswordInput = form.getInputByName("password")
        passwordInput.type("Test1234")
        val pageResult: HtmlPage = form.getButtonByName("submit").click()
        webClient.waitForBackgroundJavaScript(500)
        then(pageResult.getElementById("login-response").textContent).isEqualTo("UnknownSupabaseException")
//        TODO: Change mock to match supabase api? https://github.com/supabase-community/supabase-kt/issues/362

        passwordInput.type("password")
        val pageResult2: HtmlPage = form.getButtonByName("submit").click()
        webClient.waitForBackgroundJavaScript(500)
        then(pageResult2.getElementById("login-response").textContent).isEqualTo("UnknownSupabaseException")

    }

}