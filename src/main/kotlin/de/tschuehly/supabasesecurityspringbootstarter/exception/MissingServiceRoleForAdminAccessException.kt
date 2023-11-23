package de.tschuehly.supabasesecurityspringbootstarter.exception

import io.github.jan.supabase.exceptions.UnauthorizedRestException

class MissingServiceRoleForAdminAccessException(message: String, cause: UnauthorizedRestException) : Exception(message, cause) {

}
