# `@Authorized` → Spring Security Method Security Migration Guide

Starting with **OpenMRS Platform 3.0.0**, `org.openmrs.annotation.Authorized` is **deprecated**.
Spring Security's `@PreAuthorize`, `@PostAuthorize` and `@PostFilter` are the preferred and more
robust way to enforce authorization. Both `hasAuthority(...)` and OpenMRS's `hasPermission(...)`
resolve a privilege through `Context.hasPrivilege(String)` — see
[`hasPermission` or `hasAuthority`?](#haspermission-or-hasauthority) for which to write.

- **New code** should use the Spring Security annotations.
- **Existing code** is being migrated gradually. `@Authorized` is still fully enforced and is not
  scheduled for removal in 3.0.0, so nothing has to be converted in a hurry — but prefer the Spring
  Security form whenever a method is being worked on anyway.
- By convention a method is guarded by one mechanism or the other, **never both**. The two are
  enforced by separate, independent advisors (`org.openmrs.aop.AuthorizationAdvice` and Spring
  Security's method security, wired in `org.openmrs.security.OpenmrsSecurityConfig`), which is what
  lets them coexist for as long as the migration takes — and also means a method carrying both has
  to satisfy both.

## Why the Spring Security Annotations Are Preferred

`@Authorized` can express exactly two things: *any one of* a flat list of privileges, or *all* of
them. The Spring Security annotations can express everything it can, plus:

- **Composable expressions.** SpEL combines checks freely with `and`, `or` and `not`, so a method
  whose rule is not a flat privilege list can state its real rule.
- **Access to the invocation.** The expression can see the method's arguments and its result
  (`#p0`, `returnObject`, `filterObject`), so authorization can depend on *what* is being accessed,
  not only on which privileges the caller holds.
- **Post-invocation filtering.** `@PostFilter` removes the elements a caller is not entitled to see
  from a returned collection, instead of the all-or-nothing outcome a pre-invocation privilege
  check is limited to.
- **One standard mechanism.** It is the documented Spring Security mechanism rather than an
  OpenMRS-specific one, and it is the same mechanism that guards URLs
  (`org.openmrs.web.security.AuthorizedUrlMatcher`), so a privilege name means the same thing
  wherever it appears.

## Translation Table

| `@Authorized` | Spring Security equivalent |
|---|---|
| `@Authorized({"Get Users"})` | `@PreAuthorize("hasAuthority('Get Users')")` |
| `@Authorized({"Get Users", "Add Users"})` | `@PreAuthorize("hasAuthority('Get Users') or hasAuthority('Add Users')")` |
| `@Authorized(value = {"Get Users", "Add Users"}, requireAll = true)` | `@PreAuthorize("hasAuthority('Get Users') and hasAuthority('Add Users')")` |
| `@Authorized()` (no privilege named) | `@PreAuthorize("isAuthenticated()")` |
| no annotation (deliberately unguarded) | no annotation |

Keep using `PrivilegeConstants` rather than literals — the expression is a compile-time constant,
so ordinary string concatenation works:

```java
// Before
@Authorized({ PrivilegeConstants.GET_PROVIDERS })
Provider getProvider(Integer providerId);

// After
@PreAuthorize("hasAuthority('" + PrivilegeConstants.GET_PROVIDERS + "')")
Provider getProvider(Integer providerId);
```

`ProviderService.getProvider(Integer)` and `UserService.getUser(Integer)` are the converted
reference examples in core.

### `hasPermission` or `hasAuthority`?

Both resolve a privilege identically — `hasAuthority('Get Users')` is routed through
`Context.hasPrivilege(String)` by `OpenmrsAuthorizationManagerFactory`, exactly as
`hasPermission(null, 'Get Users')` is by `OpenmrsPermissionEvaluator` — so neither is wrong.

Use **`hasAuthority`** for a bare privilege check, and **`hasPermission`** when the expression names
what is being accessed (`returnObject`, `filterObject`, `#someArg`), since only that form is handed
a target. `hasPermission(null, …)` says the same thing as `hasAuthority` the long way round.

## Choosing Between `@PreAuthorize`, `@PostAuthorize` and `@PostFilter`

| Need | Annotation |
|---|---|
| A privilege (or any rule not needing the result) decides the call | `@PreAuthorize` |
| The decision depends on the object the method returns | `@PostAuthorize` |
| Elements the caller may not see must be dropped from a returned collection | `@PostFilter` |
| The method **writes** anything | `@PreAuthorize` — see below |

**A method that writes must be guarded pre-invocation.** Method security runs *outside* the
transaction boundary (`@EnableMethodSecurity(offset = -700)`, see `OpenmrsSecurityConfig`), which is
what lets `@PostAuthorize`/`@PostFilter` see the real, uncached result — but it also means the
transaction has already committed by the time a post-invocation check denies. A `@PostAuthorize`
denial does **not** roll the write back. `@PreAuthorize` is unaffected: it runs before the
transaction is opened, the same guarantee `@Authorized` gave.

**A `@Cacheable` method that is `@PostFilter`ed must key its cache by the caller.** `@PostFilter`
filters the returned collection *in place*, so a shared cache entry is permanently stripped to
whatever the first caller was entitled to see. Use
`@Cacheable(keyGenerator = UserKeyGenerator.BEAN_NAME)`, or a `key` expression that includes the
user. Privileges lent for the duration of a call with `Context.addProxyPrivilege(String)` are not
part of that key, so a `@PostFilter` whose outcome depends on a proxy privilege is not safe to
cache at all.

## What Does Not Change

Migrating a method does not change which callers are authorized. Both mechanisms resolve a
privilege through `Context.hasPrivilege(String)`, so all of the following behave identically:

- superuser status, and the implicit `Anonymous`/`Authenticated` roles
- proxy privileges added with `Context.addProxyPrivilege(String)`
- the `Daemon` thread bypass
- privileges with no `Privilege` row in the database
- case-insensitive privilege and role name matching

`PreAuthorizeConversionEquivalenceTest` pins exactly this for the methods converted so far: each
is allowed for the default test context and denied once logged out, as it was under `@Authorized`.

`hasRole(...)`/`hasAnyRole(...)` are routed through `User.hasRole(String)` the same way, including
its superuser bypass. Spring's `ROLE_` prefix convention is accepted but optional: `hasRole('X')`
and `hasRole('ROLE_X')` both mean the OpenMRS role named `X`.

## What Does Change

| | `@Authorized` | Spring Security |
|---|---|---|
| Exception on denial | `AccessDeniedException` — as of 3.0.0, same as Spring Security | `AuthorizationDeniedException`, a subclass of `org.springframework.security.access.AccessDeniedException` |
| Denial message | names the missing privilege (`error.privilegesRequired`) | same, via `PrivilegeNamingAuthorizationManager` |
| Relative to the transaction | before it opens | before it opens for `@PreAuthorize`; after commit for `@PostAuthorize`/`@PostFilter` |

So converting a method no longer changes what its callers catch. That is a 3.0.0 change in its own
right: `AuthorizationAdvice` denied with `APIAuthenticationException` before, and that exception is
now deprecated. Code that maps authorization failures by exception type — webservices.rest's
`BaseRestController` and legacyui's error handling among them — has to handle `AccessDeniedException`,
whichever mechanism guards the method. `ExceptionUtil.rethrowAPIAuthenticationException` recognizes
both, so a module that still throws the deprecated exception keeps working.

## Step-by-Step

1. Replace the `@Authorized` annotation using the translation table above; remove the now-unused
   `org.openmrs.annotation.Authorized` import and add
   `org.springframework.security.access.prepost.PreAuthorize`.
2. Decide whether the rule really is a flat privilege check. If the method filters or decides on
   what it returns, this is the moment to express that with `@PostAuthorize`/`@PostFilter` — but
   not on a method that writes.
3. Keep the method's
   `@throws org.springframework.security.access.AccessDeniedException` clause; every guarded service
   method carries one.
4. Add the converted method to `PreAuthorizeConversionEquivalenceTest` (or give it an equivalent
   test of its own): allowed for the default test context, denied once logged out. Tests asserting
   on the denial need no change, since both mechanisms deny with `AccessDeniedException` — core's
   tests assert on that supertype rather than on `AuthorizationDeniedException`.
5. Run `mvn -pl api test` (plus `-pl web` if a web class is involved) and `mvn spotless:apply`.

## Pitfalls

| Pitfall | Why | Fix |
|---|---|---|
| `#someArgName` silently evaluates to `null` | the build does not compile with `-parameters`, and service annotations sit on interfaces | refer to arguments positionally as `#p0`, or name them with `@P("someArgName")` |
| A write survives a `@PostAuthorize` denial | method security runs outside the transaction boundary | guard writes with `@PreAuthorize` |
| A `@PostFilter`ed `@Cacheable` method serves one user's filtered view to everyone | `@PostFilter` filters in place | key the cache by the caller (`UserKeyGenerator.BEAN_NAME`) |
| A module's error handling stops recognizing a denial | `@Authorized` denies with `AccessDeniedException` as of 3.0.0, not `APIAuthenticationException` | handle `AccessDeniedException`; `ExceptionUtil.rethrowAPIAuthenticationException` covers both |
| Both `@Authorized` and `@PreAuthorize` on one method | both advisors run, so both must pass | keep exactly one of them |
| `@Authorized` on a class instead of a method | `AuthorizationAdvice` only resolves method-level annotations | annotate methods; `@PreAuthorize` may be placed on the type if a class-wide default is wanted |

## Further Reading

- `org.openmrs.annotation.Authorized` — the deprecation notice and the equivalences, in javadoc
- `org.openmrs.security.OpenmrsPermissionEvaluator` — what `hasPermission(...)` does
- `org.openmrs.security.OpenmrsAuthorizationManagerFactory` — what `hasAuthority(...)`/`hasRole(...)` do
- `org.openmrs.security.OpenmrsSecurityConfig` — interceptor ordering, and why it is what it is
- [Spring Security: Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [Spring Security: Expression-Based Access Control](https://docs.spring.io/spring-security/reference/servlet/authorization/expression-based.html)
