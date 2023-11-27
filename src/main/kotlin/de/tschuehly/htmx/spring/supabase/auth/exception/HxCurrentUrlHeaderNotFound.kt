package de.tschuehly.htmx.spring.supabase.auth.exception

class HxCurrentUrlHeaderNotFound : Exception("No HX-Current-URL header found while calling the /jwt endpoint")
