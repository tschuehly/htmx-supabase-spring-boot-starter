package de.tschuehly.htmx.spring.supabase.auth.test.mock

import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class GoTrueMock {

    val engine = MockEngine {
        handleRequest(it) ?: respondInternalError("Invalid route")
    }

    private suspend fun MockRequestHandleScope.handleRequest(request: HttpRequestData): HttpResponseData? {
        val url = request.url
        val urlWithoutQuery = url.encodedPath
        return when {
            urlWithoutQuery.endsWith("token") -> handleLogin(request)
            urlWithoutQuery.endsWith("signup") -> handleSignUp(request)
            urlWithoutQuery.endsWith("user") -> handleUserRequest(request)
            urlWithoutQuery.endsWith("verify") -> handleVerify(request)
            urlWithoutQuery.endsWith("otp") -> handleOtp(request)
            urlWithoutQuery.endsWith("recover") -> handleRecovery(request)
            urlWithoutQuery.endsWith("logout") -> handleLogout(request)
            else -> null
        }
    }

    private fun MockRequestHandleScope.handleLogout(request: HttpRequestData): HttpResponseData {
        if (request.method != HttpMethod.Post) return respondBadRequest("Invalid method")
        if (!request.headers.contains("Authorization")) return respondBadRequest("access token missing")
        return respondOk()
    }

    private suspend fun MockRequestHandleScope.handleRecovery(request: HttpRequestData): HttpResponseData {
        if (request.method != HttpMethod.Post) respondBadRequest("Invalid method")
        val body = try {
            request.decodeJsonObject()
        } catch (e: Exception) {
            return respondBadRequest("Invalid body")
        }
        if (!body.containsKey("email")) return respondBadRequest("Email missing")
        return respondOk()
    }

    private suspend fun MockRequestHandleScope.handleOtp(request: HttpRequestData): HttpResponseData {
        if (request.method != HttpMethod.Post) respondBadRequest("Invalid method")
        val body = try {
            request.decodeJsonObject()
        } catch (e: Exception) {
            return respondBadRequest("Invalid body")
        }
        if (!body.containsKey("create_user")) return respondBadRequest("create_user missing")
        if (!body.containsKey("phone") && !body.containsKey("email")) return respondBadRequest("email or phone missing")
        return respondOk("{}")
    }

    private suspend fun MockRequestHandleScope.handleVerify(request: HttpRequestData): HttpResponseData {
        if (request.method != HttpMethod.Post) return respondBadRequest("Invalid method")
        val body = try {
            request.decodeJsonObject()
        } catch (e: Exception) {
            return respondBadRequest("Invalid body")
        }
        if (!body.containsKey("token")) return respondBadRequest("token missing")
        if (!body.containsKey("type")) return respondBadRequest("type missing")
        val token = body["token"]!!.jsonPrimitive.content
        if (token != VALID_VERIFY_TOKEN) return respondBadRequest("Failed to verify user")
        return when (body["type"]!!.jsonPrimitive.content) {
            in listOf("invite", "signup", "recovery") -> respondValidSession()
            "sms" -> {
                body["phone"]?.jsonPrimitive?.contentOrNull
                    ?: return respondBadRequest("Missing parameter: phone_number")
                respondValidSession()
            }

            else -> respondBadRequest("Invalid type")
        }
    }

    private fun MockRequestHandleScope.handleUserRequest(request: HttpRequestData): HttpResponseData {
        if (!request.headers.contains(HttpHeaders.Authorization)) return respondUnauthorized()
        val authorizationHeader = request.headers[HttpHeaders.Authorization]!!
        if (!authorizationHeader.startsWith("Bearer ")) return respondUnauthorized()
        val token = authorizationHeader.substringAfter("Bearer ")
        if (token != VALID_ACCESS_TOKEN) return respondUnauthorized()
        return when (request.method) {
            HttpMethod.Get -> respondJson(UserInfo(aud = "", id = "userid"))
            HttpMethod.Put -> {
                respondJson(
                    UserInfo(
                        aud = "",
                        id = "userid",
                        email = "old_email@email.com",
                        emailChangeSentAt = Clock.System.now()
                    )
                )
            }

            else -> return respondBadRequest("Invalid method")
        }
    }

    private suspend fun MockRequestHandleScope.handleSignUp(request: HttpRequestData): HttpResponseData {
        if (request.method != HttpMethod.Post) respondBadRequest("Invalid method")
        val body = try {
            request.decodeJsonObject()
        } catch (e: Exception) {
            return respondBadRequest("Invalid body")
        }

        return when {
            body.containsKey("email") -> {
                respond(
                    sampleUserObject(body["email"]!!.jsonPrimitive.content),
                            HttpStatusCode.OK,
                    headersOf("Content-Type" to listOf("application/json"))

                )
            }

            body.containsKey("phone") -> {
                respond(
                    sampleUserObject(body["phone"]!!.jsonPrimitive.content),
                    HttpStatusCode.OK,
                    headersOf("Content-Type" to listOf("application/json"))
                )
            }

            !body.containsKey("password") -> respondBadRequest("Missing password")
            else -> respondBadRequest("Missing email or phone")
        }
    }

    private suspend fun MockRequestHandleScope.handleLogin(request: HttpRequestData): HttpResponseData {
        if (request.method != HttpMethod.Post) respondBadRequest("Invalid method")
        if (!request.url.parameters.contains("grant_type")) return respondBadRequest("grant_type is required")
        return when (request.url.parameters["grant_type"]) {
            "refresh_token" -> {
                val body = try {
                    request.decodeJsonObject()
                } catch (e: Exception) {
                    return respondBadRequest("Invalid body")
                }
                if (!body.containsKey("refresh_token")) return respondBadRequest("refresh_token is required")
                val refreshToken = body["refresh_token"]!!.jsonPrimitive.content
                if (refreshToken != VALID_REFRESH_TOKEN) return respondBadRequest("Invalid refresh token")
                respondValidSession()
            }

            "password" -> {
                val body = try {
                    request.decodeJsonObject()
                } catch (e: Exception) {
                    return respondBadRequest("Invalid body")
                }
                if (!body.containsKey("password")) return respondBadRequest("password is required")
                val password = body["password"]?.jsonPrimitive?.contentOrNull ?: ""
                return when {
                    body.containsKey("email") -> {
                        if (password != VALID_PASSWORD) return respondBadRequest("Invalid password")
                        respondValidSession()
                    }

                    body.containsKey("phone") -> {
                        if (password != VALID_PASSWORD) return respondBadRequest("Invalid password")
                        respondValidSession()
                    }

                    else -> respondBadRequest("email or phone is required")
                }
            }

            else -> respondBadRequest("grant_type must be password")
        }
    }

    private inline fun <reified T> MockRequestHandleScope.respondJson(data: T): HttpResponseData {
        return respond(
            Json.encodeToString(data),
            HttpStatusCode.OK,
            headersOf("Content-Type" to listOf("application/json"))
        )
    }

    private fun MockRequestHandleScope.respondValidSession() = respondJson(
        UserSession(
            NEW_ACCESS_TOKEN,
            "refresh_token",
            "",
            "",
            200,
            "token_type",
            UserInfo(aud = "", id = "")
        )
    )

    private fun MockRequestHandleScope.respondInternalError(message: String): HttpResponseData {
        return respond(message, HttpStatusCode.InternalServerError)
    }

    private fun MockRequestHandleScope.respondBadRequest(message: String): HttpResponseData {
        return respond(buildJsonObject {
            put("error", message)
        }.toString(), HttpStatusCode.BadRequest)
    }

    private fun MockRequestHandleScope.respondUnauthorized(): HttpResponseData {
        return respond("Unauthorized", HttpStatusCode.Unauthorized)
    }

    private suspend inline fun HttpRequestData.decodeJsonObject() =
        Json.decodeFromString<JsonObject>(body.toByteArray().decodeToString())

    companion object {
        const val VALID_PASSWORD = "password"
        const val VALID_REFRESH_TOKEN = "valid_refresh_token"
        const val NEW_ACCESS_TOKEN = "new_access_token"
        const val VALID_ACCESS_TOKEN = "valid_access_token"
        const val VALID_VERIFY_TOKEN = "valid_verify_token"
    }

    private fun sampleUserObject(email: String? = null, phone: String? = null) = """
        {
            "id": "id",
            "aud": "aud",
            "email": "$email",
            "phone": "$phone"
        }
    """.trimIndent()

}