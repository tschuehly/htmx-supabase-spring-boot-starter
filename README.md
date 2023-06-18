# Supabase Security Spring Boot Starter

This Spring Starter is aimed at Developers that want to simplify Spring Security and
integrate [Supabase](https://supabase.com/) into their Project.

Supabase gives us access to two important things for free:

- Hosted Postgres Server with 500 MB Database Storage
- Integrated GoTrue API for Authentication/Authorization of up to 50.000 Monthly Active Users

## Features of supabase-security-spring-boot-starter

- Spring Security Configuration with application.yaml/properties
- Integration with htmx
- Role-Based Access Control

## Known Issues:

Access Denied

## Initial Setup:

Go to [supabase.com](https://app.supabase.com/sign-up) and sign up for an account.
Create a new Supabase project. Save your database password for later.

Go to [start.spring.io](https://start.spring.io/) and create a new Spring Boot project.

Include the dependency in your build.gradle.kts. You can look up the newest version
on [search.maven.org](https://search.maven.org/artifact/de.tschuehly/supabase-security-spring-boot-starter)

````kotlin
dependencies {
    implementation("de.tschuehly:supabase-security-spring-boot-starter:0.2.2")
}
````

Go to your Spring App and configure your application.yaml/properties.
You can find them at Project Settings -> API or `https://app.supabase.com/project/yourProjectId/settings/api`

```yaml
supabase:
  projectId: yourProjectId
  anonKey: ${SUPABASE_ANON_KEY}
  databasePassword: ${SUPABASE_DATABASE_PW}
  jwtSecret: ${SUPABASE_JWT_SECRET}
  successfulLoginRedirectPage: "/account"
  passwordRecoveryPage: /updatePassword
  unauthenticatedPage: /unauthenticated
  unauthorizedPage: /unauthorizedPage
  sslOnly: false
```

``anonKey``, ``databasePassword`` and ``jwtSecret`` are sensitive properties, you should set these with environment
variables.

You need to set the Site URL and the Redirect URL in your supabase dashboard as well.
You can find them at Authentication -> URL Configuration.
If you didn't mess with the ``server.port`` property you should set it to `http://localhost:8080`

Now you can get started with integrating Authentication. The Supabase Postgres Database is automatically configured
for you.

## Configuring Public Authorization

You can configure public accessible paths in your application.yaml with the property `supabase.public`. You can
configure access for get,post,put,delete

```yaml
supabase:
  public:
    get:
      - "/"
      - "/logout"
      - "/login"
      - "/403"
      - "/favicon.ico"
      - "/error"
      - "/resetPassword"
      - "/unauthenticated"
      - "/unauthorized"
    post:
      - "/api/user/register"
      - "/api/user/login"
      - "/api/user/jwt"
      - "/api/user/sendPasswordResetEmail"
```

## Basic Usage with HTMX

This Library is heavily optimized for [HTMX](https://htmx.org/), an awesome Library to
build [modern user interfaces](https://htmx.org/examples) with the simplicity and power of hypertext. htmx gives you
access
to [AJAX](https://htmx.org/docs#ajax), [CSS Transitions](https://htmx.org/docs#css_transitions), [WebSockets](https://htmx.org/docs#websockets)
and [Server Sent Events](https://htmx.org/docs#sse) directly in HTML,
using [attributes](https://htmx.org/reference#attributes)

You need to add this little script snippet to your index page. This will authenticate with the API after logging in with
Google / confirm your email

````html
<script>
    if (window.location.hash.startsWith("#access_token")) {
        htmx.ajax('POST', '/api/user/jwt', {target: '#body', swap: 'outerHTML'})
            .then(window.location.hash = "")
    }
</script>
````

### Register

````html
<form>
    <label>Email:
        <input type="text" name="email"/>
    </label>
    <label>Password:
        <input type="password" name="password"/>
    </label>
    <button hx-post="/api/user/register">Submit</button>
</form>
````

You should get an email with a confirmation link and if we click on that we get redirected to the page we specified with
the property: `supabase.successfulLoginRedirectPage: "/account"`

### Login with E-Mail

````html
<form>
    <label>Email:
        <input type="text" name="email"/>
    </label>
    <label>Password:
        <input type="password" name="password"/>
    </label>
    <button hx-post="/api/user/login">Submit</button>
</form>
````

### Login with Social Provides

You need to configure each social provider in your supabase dashboard at Authentication -> Configuration -> Providers

#### Google

If you configured Google you can just insert a link to log in with Google

````html
<a href="https://<projectId>.supabase.co/auth/v1/authorize?provider=google">Sign In with Google</a>
````

### Logout

````html
<h2>
    <button hx-get="/api/user/logout">Logout</button>
</h2>
````

## Role-based access control

You can create a role-based access control configuration right inside your application.yaml:

```yaml
supabase:
  roles:
    admin:
      get:
        - "/admin/**"
    user:
      post:
        - "/user-feature-1/**"
```

With this configuration users with the Authority ROLE_ADMIN can access any endpoints under the /admin/** path, and any user with the Authority ROLE_USER can create POST request to the endpoints under the /user-feature-1/** path.

You need to be able to set roles for a user but there are two ways to do that:

### service role JWT

When you go to your supabase project in the Project Settings -> API section you can find the service_role secret. With
this secret, you can set the role for any user. This way you can control user roles directly from your backend.

Here is an example curl request that sets the role of the user with the id: `381c6358-22dd-4681-81e3-c79846117511` to `USER`

````shell
curl -X PUT --location "http://localhost:8080/api/user/setRoles" \
-H "Cookie: JWT=service_role_secret" \
-H "Content-Type: application/x-www-form-urlencoded; charset=UTF-8" \
-d "userId=381c6358-22dd-4681-81e3-c79846117511&roles=user"
````

### Superuser account

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

After executing this SQL, this account can set the roles of other users with this form:

````html

<form>
    <label>User Id
        <input name="userId" type="text">
    </label>
    <label>Admin Role
        <input name="roles" type="checkbox" value="admin"/>
    </label>
    <label>User Role
        <input name="roles" type="checkbox" value="user"/>
    </label>
    <button hx-put="/api/user/setRoles">Submit</button>
</form>

````

## Basic Auth

Some applications need to be configured with Basic Authentication, for example Prometheus does not support cookie based authentication.

You can configure Basic Authentication using the supabase.basicAuth property.
Then encrypt the password using the [Spring Boot CLI](https://docs.spring.io/spring-boot/docs/current/reference/html/cli.html)

```yaml
supabase:
  basicAuth:
    enabled: true
    username: prometheus
    password: "{bcrypt}$2a$$LVUNCy8Lht68w7KA0nobWuwyzbW8AdF3bRC25glv7M12ACAZ4PT8u"
    roles:
      - "ADMIN"
```