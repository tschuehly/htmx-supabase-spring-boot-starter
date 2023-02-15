# Supabase Spring Boot Starter

This Repository is aimed at Spring Developers that want to simplify Spring Security and
integrate [Supabase](https://supabase.com/) into their Project.

Supabase gives us access to two important things for free:

- Hosted Postgres Server with 500 MB Database Storage
- Integrated GoTrue Api for Authentication/Authorization up to 50.000 MAU

## Features of supabase-spring-boot-starter

- Spring Security Configuration with your application.yaml/properties
- Role Based Access Control

## Initial Setup:

Go to [supabase.com](https://app.supabase.com/sign-up) and sign up for an account.
Create a new supabase project. Save your database password for later.

Go to [start.spring.io](https://start.spring.io/) and create a new Spring Boot project.

Include the dependency in your build.gradle.kts. You can look up the newest version
on [jitpack.io](https://jitpack.io/#tschuehly/supabase-spring-boot-starter)

````kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.tschuehly:supabase-spring-boot-starter:75b3c6299a")
}
````

Go to your Spring App and configure your application.yaml/properties

`````yaml
supabase:
  projectId: yourProjectKey
  anonKey: ${SUPABASE_ANON_KEY}
  databasePassword: ${SUPABASE_DATABASE_PW}
  jwtSecret: ${SUPABASE_JWT_SECRET}
  successfulLoginRedirectPage: "/account"
  passwordRecoveryPage: "/requestPasswordReset"
  sslOnly: false
`````

anonKey, databasePassword and jwtSecret are sensitive properties, you should set these with environment variables.

Now you can get started with integrating the Authentication. The Supabase Postgres Database is automatically configured
for you.

## Basic Usage with HTMX

## Basic Usage as JSON API

## Role Based Access Control

When you want to use Role Based Access Control you need to be able to set roles for a user there are two ways to do
that:

### service_role jwt

When you go to your supabase project in the settings/api section you can find the service_role secret. With this secret
you can set the role for any user.

````shell
curl -X PUT --location "http://localhost:8080/api/user/setRoles" \
-H "Cookie: JWT=service_role_secret" \
-H "Content-Type: application/x-www-form-urlencoded; charset=UTF-8" \
-d "userId=381c6358-22dd-4681-81e3-c79846117511&roles=user"
````

### superuser account

You can also elevate a normal user account that will act as a "superuser" account.

To do this you need to set the role of the user to "service_role" in the auth.users table;

You can do this with the following SQL:

````sql
update auth.users
set role = 'service_role'
where email = 'test@example.com';

select *
from auth.users;
````

