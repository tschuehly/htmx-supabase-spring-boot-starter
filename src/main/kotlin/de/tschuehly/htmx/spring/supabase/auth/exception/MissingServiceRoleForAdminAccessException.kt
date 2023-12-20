package de.tschuehly.htmx.spring.supabase.auth.exception

class MissingServiceRoleForAdminAccessException(userId: String) :
    Exception("User with id: $userId has tried to setRoles without having service_role") {

}
