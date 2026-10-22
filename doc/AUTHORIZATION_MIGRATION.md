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
  (`#argName`, `returnObject`, `filterObject`), so authorization can depend on *what* is being
  accessed, not only on which privileges the caller holds. `#argName` is resolved against the
  **implementation** method rather than the annotated interface method, so both sides have to agree
  on the parameter name - see Pitfalls. (Resolution also needs the build's
  `<parameters>true</parameters>`, since Spring no longer falls back to debug symbols.)
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
| `@Authorized({"Get Users"})`, nothing in the signature identifying what is accessed | `@PreAuthorize("hasAuthority('Get Users')")` |
| `@Authorized({"Get Users"})` on a method taking the **object** | `@PreAuthorize("hasPermission(#user, 'Get Users')")` |
| `@Authorized({"Get Users"})` on a method taking an **id or uuid** | `@PreAuthorize("hasPermission(#userId, 'User', 'Get Users')")` |
| `@Authorized({"Get Users", "Add Users"})` | `@PreAuthorize("hasAuthority('Get Users') or hasAuthority('Add Users')")` |
| `@Authorized(value = {"Get Users", "Add Users"}, requireAll = true)` | `@PreAuthorize("hasAuthority('Get Users') and hasAuthority('Add Users')")` |
| `@Authorized()` (no privilege named) | `@PreAuthorize("isAuthenticated()")` |
| no annotation (deliberately unguarded) | no annotation |

Keep using `PrivilegeConstants` rather than literals — the expression is a compile-time constant,
so ordinary string concatenation works:

```java
// Before
@Authorized({ PrivilegeConstants.GET_USERS })
User getUser(Integer userId);

// After
@PreAuthorize("hasPermission(#userId, 'User', '" + PrivilegeConstants.GET_USERS + "')")
User getUser(Integer userId);
```

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
- the `Daemon` thread bypass - registered `DomainObjectAuthorizationRule`s are skipped there too,
  since system work runs as no user and a rule written against the current user would deny it
- privileges with no `Privilege` row in the database
- case-insensitive privilege and role name matching

`PreAuthorizeConversionEquivalenceTest` pins exactly this for the four reference methods it
names: each is allowed for the default test context and denied once logged out, as it was under
`@Authorized`.

That covers *converting* a guard. Adding one where there was none is a different matter, and this
migration does it for six methods reaching patient data that `@Authorized` left open:
`ConditionService.getConditionsByEncounter`, `DiagnosisService.getPrimaryDiagnoses` and
`getUniqueDiagnoses`, `EncounterService.getAllEncounters(Cohort)`,
`PatientService.mergePatients(Patient, List)`, and `RefByUuid.getRefByUuid` - the first five on the
privilege their own service already required elsewhere, the last on authentication alone. A role
that reached one of them without that privilege no longer can, and the routes worth checking on
upgrade are the indirect ones: a module wrapping one of these in a service of its own, without a
guard of its own, passes the new requirement straight through to its callers.

`hasRole(...)`/`hasAnyRole(...)` are routed through `User.hasRole(String)` the same way, including
its superuser bypass. Spring's `ROLE_` prefix convention is accepted but optional: `hasRole('X')`
and `hasRole('ROLE_X')` both mean the OpenMRS role named `X`.

## What Does Change

| | `@Authorized` | Spring Security |
|---|---|---|
| Exception on denial | `APIAuthenticationException`, which as of 3.0.0 is a subclass of `AuthorizationDeniedException` | the same `APIAuthenticationException` when the denial names the missing privilege or `isAuthenticated()` denied, a plain `AuthorizationDeniedException` otherwise; both are `org.springframework.security.access.AccessDeniedException`s |
| Denial message | names the missing privilege (`error.privilegesRequired`); a no-value `@Authorized` says authentication is required (`error.aunthenticationRequired`) | the same, via `PrivilegeNamingAuthorizationManager`, for a missing privilege or a denying `isAuthenticated()`; otherwise "Access Denied: " followed by the denying expression |
| Relative to the transaction | before it opens | before it opens for `@PreAuthorize`; after commit for `@PostAuthorize`/`@PostFilter` |

So converting a method by the translation table does not change what its callers catch.
`APIAuthenticationException` is deprecated as of 3.0.0 but still thrown, now as an
`AccessDeniedException` and no longer an `APIException`: code written before 3.0.0 that catches it
keeps recognizing the denials it did, and new code catches `AccessDeniedException`. Only the latter
catches every denial, since an expression that denies for a reason other than a missing privilege
or authentication — `denyAll()`, for one — throws a plain `AuthorizationDeniedException`, so code
that maps authorization failures by exception type — webservices.rest's `BaseRestController` and
legacyui's error handling among them — should handle `AccessDeniedException`.
`ExceptionUtil.rethrowAPIAuthenticationException` recognizes any `AccessDeniedException`, so a
module that still throws the deprecated exception keeps working.

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
   on the denial need no change, since a method converted by the translation table denies with the
   same exception and message.
5. Run `mvn -pl api test` (plus `-pl web` if a web class is involved) and `mvn spotless:apply`.

## Pitfalls

| Pitfall | Why | Fix |
|---|---|---|
| `#someArgName` silently evaluates to `null`, so no rule is consulted | argument names resolve against the **implementation** method (`AopUtils.getMostSpecificMethod`), not the annotated interface method, so a name only the interface declares is unknown - and `hasPermission(null, privilege)` is a plain privilege check | make the implementation's parameter name match, or annotate that parameter `@P("someArgName")`; `MethodSecurityExpressionArgumentNameTest` fails the build on a mismatch |
| `hasPermission(#someCollection, 'Type', privilege)` throws `ClassCastException` | the 3-argument overload binds its target to `Serializable`, and `List.subList(...)` and similar views are not | for a collection of *objects* use the 2-argument form, `hasPermission(#whom, privilege)`, which judges each element by its own type and never binds to `Serializable`. A collection of *ids* belongs in the 3-argument form (each id is judged against the named type), but the collection itself still has to be `Serializable` |
| `hasPermission(#someIds, privilege)` silently consults no rule | the 2-argument form judges each element by its *own* type, so a `List<Integer>` of patient ids resolves rules for `Integer` and a `Patient` rule never runs - nothing is checked and the call authorizes | name the type: `hasPermission(#someIds, 'Patient', privilege)`. Omitting it is not a laxer check, it is no check |
| A rule for a *subtype* is not consulted | a rule covers its target type and everything assignable to it, so `Person.class` reaches a `Patient` - but the reverse cannot work: where only the supertype is known (a declared type, or an uninitialized Hibernate proxy, which is read without initializing it) a `DrugOrder.class` rule cannot be matched to an `Order` | register the rule for the type actually named at the call site, or for the supertype if it should cover the whole hierarchy |
| A `@PostFilter` over a wrapper type reaches no rule for what it wraps | a rule is selected from the element's own runtime type, and `Concept.class` is not assignable from `ConceptSearchResult`, so the filter collapses to the privilege check `@PreAuthorize` already made | name what the wrapper holds: `hasPermission(filterObject.concept, privilege)`, the same way a `Map` entry names `filterObject.key` |
| A rule reads a natural key as a uuid | a `String` `targetId` is a uuid everywhere but `GlobalProperty`, whose methods name the property name (`getGlobalProperty(String)` and friends) so that a rule can hide named properties; `getGlobalPropertyByUuid(String)` is the one that names a uuid, and nothing tells the two apart | match a `String` against both forms and deny if either is denied. Denying the form the rule did not expect would break `getGlobalProperty` for everyone, which `PersonName.getFullName()` resolves a name format through |
| A `targetType` name silently matches no rule | `hasPermission(#id, 'Type', privilege)` resolves a simple name against `org.openmrs` only; anything elsewhere needs its fully-qualified name (`'org.openmrs.hl7.HL7InQueue'`). A name that loads no class is logged as a warning and leaves the call authorized on the privilege alone | qualify the name, and check the log for `No class '...' for the targetType` |
| A criteria-object overload bypasses a rule the parameter-list overload enforces | `getObservations(ObsSearchCriteria)` is a second route to the same query | name the criteria's own subject: `hasPermission(#obsSearchCriteria?.whom, privilege)`, with `?.` so a null criteria stays a privilege-only check instead of failing in SpEL |
| A rule loading its target recurses until the stack overflows | the id form of a rule runs *before* the fetch, so fetching the object through the service method being authorized re-enters the same check | read it through a DAO or the Hibernate session, or guard the re-entrant call with a thread-local |
| A rule handling only `Integer` ids lets uuid lookups through | a rule covers both id forms of its type, and `getXByUuid` is how REST loads an object | handle every id form the type is named with, or deny the ones the rule cannot interpret |
| A write survives a `@PostAuthorize` denial | method security runs outside the transaction boundary | guard writes with `@PreAuthorize` |
| A `@PostFilter`ed `@Cacheable` method serves one user's filtered view to everyone | `@PostFilter` filters in place | key the cache by the caller (`UserKeyGenerator.BEAN_NAME`) |
| A module's error handling misses a denial | an expression that denies for a reason other than a missing privilege or authentication, such as `denyAll()`, throws a plain `AuthorizationDeniedException`, not `APIAuthenticationException` | handle `AccessDeniedException`, which every denial is; `ExceptionUtil.rethrowAPIAuthenticationException` covers it |
| A module class fails to load, or OpenMRS fails to start, with `java.lang.VerifyError: Stack map does not match the one at exception handler`, because `Type 'org/openmrs/api/APIAuthenticationException' ... is not assignable to 'org/openmrs/api/APIException'` | the module was compiled against a core where `APIAuthenticationException` extended `APIException`: `catch (APIAuthenticationException \| ContextAuthenticationException e)` types its handler as their common supertype, `APIException` | rebuild the module against 3.0.0, and catch `AccessDeniedException` there instead |
| Both `@Authorized` and `@PreAuthorize` on one method | both advisors run, so both must pass | keep exactly one of them |
| `@Authorized` on a class instead of a method | `AuthorizationAdvice` only resolves method-level annotations | annotate methods; `@PreAuthorize` may be placed on the type if a class-wide default is wanted |

## Further Reading

- `org.openmrs.annotation.Authorized` — the deprecation notice and the equivalences, in javadoc
- `org.openmrs.security.OpenmrsPermissionEvaluator` — what `hasPermission(...)` does
- `org.openmrs.security.OpenmrsAuthorizationManagerFactory` — what `hasAuthority(...)`/`hasRole(...)` do
- `org.openmrs.security.OpenmrsSecurityConfig` — interceptor ordering, and why it is what it is
- [Spring Security: Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [Spring Security: Expression-Based Access Control](https://docs.spring.io/spring-security/reference/servlet/authorization/expression-based.html)
