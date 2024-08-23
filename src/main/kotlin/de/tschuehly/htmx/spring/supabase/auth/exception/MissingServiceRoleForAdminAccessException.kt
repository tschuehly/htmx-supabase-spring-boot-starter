package de.tschuehly.htmx.spring.supabase.auth.exception

import java.util.*

class MissingServiceRoleForAdminAccessException(userId: UUID?) :
    Exception("User with id: $userId has tried to setRoles without having service_role") {
}
