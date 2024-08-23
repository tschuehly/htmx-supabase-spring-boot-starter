package de.tschuehly.htmx.spring.supabase.auth.exception

import io.github.jan.supabase.gotrue.exception.AuthRestException

class SupabaseAuthException(exc: AuthRestException) :
    Exception("Supabase failed: ${exc.error} with message: ${exc.message}, code: ${exc.errorCode}", exc) {
    val errorCode = exc.errorCode
    val error: String = exc.error
}
