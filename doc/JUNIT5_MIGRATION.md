# JUnit 4 → JUnit 5 Migration Guide

Starting with **OpenMRS Platform 3.0.0**, all JUnit 4 support has been removed from openmrs-core.
Modules that still use JUnit 4 base test classes must migrate to the JUnit 5 (Jupiter) equivalents.

## What Changed

The following deprecated classes have been **deleted**:

| Deleted Class (JUnit 4) | Replacement (JUnit 5) |
|---|---|
| `org.openmrs.test.BaseContextSensitiveTest` | `org.openmrs.test.jupiter.BaseContextSensitiveTest` |
| `org.openmrs.test.BaseContextMockTest` | `org.openmrs.test.jupiter.BaseContextMockTest` |
| `org.openmrs.test.BaseModuleContextSensitiveTest` | `org.openmrs.test.jupiter.BaseModuleContextSensitiveTest` |
| `org.openmrs.web.test.BaseWebContextSensitiveTest` | `org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest` |
| `org.openmrs.web.test.BaseModuleWebContextSensitiveTest` | `org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest` |

The following **dependencies** have been removed from the BOM:

- `junit:junit` (JUnit 4)
- `org.junit.vintage:junit-vintage-engine` (JUnit 4 compatibility bridge)
- `org.hamcrest:hamcrest-core` (use `org.hamcrest:hamcrest` instead)

## Step-by-Step Migration

### 1. Update Base Class Imports

Replace the old base class with the `jupiter` equivalent. The API is identical:

```java
// Before
import org.openmrs.test.BaseContextSensitiveTest;

// After
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
```

Same pattern applies for all five base classes listed above.

### 2. Replace JUnit 4 Annotations with JUnit 5

| JUnit 4 | JUnit 5 |
|---|---|
| `@org.junit.Test` | `@org.junit.jupiter.api.Test` |
| `@org.junit.Before` | `@org.junit.jupiter.api.BeforeEach` |
| `@org.junit.After` | `@org.junit.jupiter.api.AfterEach` |
| `@org.junit.BeforeClass` | `@org.junit.jupiter.api.BeforeAll` |
| `@org.junit.AfterClass` | `@org.junit.jupiter.api.AfterAll` |
| `@org.junit.Ignore` | `@org.junit.jupiter.api.Disabled` |
| `@org.junit.Rule` | Use `@ExtendWith` or `@RegisterExtension` instead |
| `@RunWith(SpringRunner.class)` | `@ExtendWith(SpringExtension.class)` |

> **Note:** If your test extends one of the OpenMRS base test classes (e.g. `BaseContextSensitiveTest`),
> you do **not** need `@ExtendWith(SpringExtension.class)` — the base class already includes it.
> You only need the explicit annotation if your test configures its own Spring context independently.

### 3. Replace JUnit 4 Assertions with JUnit 5

JUnit 5 assertions are in `org.junit.jupiter.api.Assertions` and the message parameter
is now the **last** argument (not the first):

```java
// Before (JUnit 4)
import static org.junit.Assert.assertEquals;
assertEquals("message", expected, actual);

// After (JUnit 5)
import static org.junit.jupiter.api.Assertions.assertEquals;
assertEquals(expected, actual, "message");
```

### 4. Replace `@Rule ExpectedException` with `assertThrows`

```java
// Before (JUnit 4)
@Rule
public ExpectedException thrown = ExpectedException.none();

@Test
public void shouldThrow() {
    thrown.expect(APIException.class);
    thrown.expectMessage("error");
    service.doSomething();
}

// After (JUnit 5)
@Test
void shouldThrow() {
    APIException ex = assertThrows(APIException.class, () -> service.doSomething());
    assertTrue(ex.getMessage().contains("error"));
}
```

### 5. Update Hamcrest Imports

If you used `org.junit.internal.matchers.TypeSafeMatcher`, change to `org.hamcrest.TypeSafeMatcher`.
The `hamcrest-core` artifact has been replaced with `hamcrest` (which is a superset).

### 6. Remove `public` Visibility (Optional but Recommended)

JUnit 5 does not require test classes or methods to be `public`:

```java
// Before
public class MyServiceTest extends BaseModuleContextSensitiveTest {
    @Test
    public void shouldDoSomething() { ... }
}

// After
class MyServiceTest extends BaseModuleContextSensitiveTest {
    @Test
    void shouldDoSomething() { ... }
}
```

### 7. Update POM Dependencies

If your module POM explicitly declares JUnit 4 dependencies, replace them:

```xml
<!-- Remove -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <scope>test</scope>
</dependency>

<!-- Add (if not already inherited from openmrs-test) -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

## Common Errors After Upgrading

| Error | Cause | Fix |
|---|---|---|
| `cannot find symbol: class BaseContextSensitiveTest` | Old import from `org.openmrs.test` | Change to `org.openmrs.test.jupiter` |
| `package org.junit does not exist` | JUnit 4 removed | Change `org.junit.Test` → `org.junit.jupiter.api.Test`, etc. |
| `package org.junit.internal.matchers does not exist` | JUnit 4 internals removed | Use `org.hamcrest.TypeSafeMatcher` |
| `cannot find symbol: class ExpectedException` | JUnit 4 rules removed | Use `assertThrows()` |
| `method assertEquals in Assert cannot be applied` | Different parameter order | Move message string to last parameter |
| `cannot find symbol: class SpringRunner` | JUnit 4 runner removed | Use `@ExtendWith(SpringExtension.class)` or extend a base test class |

## Further Reading

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [JUnit 5 Migration Tips](https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4)
