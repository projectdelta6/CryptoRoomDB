# CryptoRoomDB: Value Class Redesign

**Purpose:** Simplify CryptoString from a wrapper class to a value class while maintaining type safety and Room compatibility.
**Status:** Proposal — **reviewed & spiked 2026-06-08; technically viable on Room 2.8.4 (see §0)**
**Impact:** Breaking change for direct `CryptoString` construction; TypeConverter *usage* unchanged, but the converter's internals and the library's own test suite need rework.

---

## 0. Review Update (2026-06-08)

This plan was written 2026-01-18, before the library gained a test suite, a Kover
coverage gate, CI, and an AGP 9 / Kotlin 2.4 / Room 2.8.4 upgrade. The core idea
(value class) still stands, but the context has changed and there is one risk the
original plan does not address.

### 0.1 ✅ RESOLVED by spike: Room uses the converter, does NOT store plaintext

The concern: Room **2.6+ natively supports `@JvmInline value class` columns by unwrapping
them** to the underlying type. Because `CryptoString` would wrap a `String` and the column
is already `String`, Room *might* map the field directly to the column and never invoke
`CryptoStringTypeConverter` — storing **plaintext** and silently defeating encryption.

**Spike result (2026-06-08):** Built a throwaway value class + non-null `@TypeConverter`
(visible reversible transform) + `@Database` in the test sources and asserted on the **raw
column**. With the converter registered, Room **invoked it and stored ciphertext** — the
raw value was `ENC:<reversed>`, not the plaintext. So a `@TypeConverter` takes precedence
over native unwrapping on **Room 2.8.4 / KSP 2.3.9**. **Not a blocker.**

> ⚠️ **New caveat the spike exposes — a safety regression.** That precedence only holds
> *while the converter is registered*. With a value class, **omitting** `@TypeConverters`
> makes Room fall back to native unwrapping and **silently store plaintext** — no error.
> Today's *class*-based `CryptoString` instead produces a loud compile error if the
> converter is missing ("cannot figure out how to save this field"). So the redesign trades
> a fail-loud failure mode for a fail-silent one. **This is now confirmed by spike and fully
> mitigated — see §0.7:** giving the value class a `private` backing property makes Room
> refuse to auto-unwrap, so a missing converter is a compile error again.

### 0.2 ✅ RESOLVED by spike: no name-mangling problem

The spike's converter used a **non-null** value-class parameter (`fromSpike(SpikeCryptoString)`),
which is exactly the case that produces a mangled JVM name (`fromSpike-<hash>`). Room/KSP
codegen resolved it and the generated `_Impl` compiled and ran. **Not an issue** on the
current toolchain — the plan can use non-null converter signatures if desired.

### 0.3 🟠 The test suite must be MIGRATED, not written fresh

§7's example tests predate the real suite. The redesign **deletes API that the current
`CryptoStringTest` covers**, so these cases must be removed or rewritten as
extension-function tests: secondary constructors (`CryptoString()`,
`CryptoString(CryptoString?)`, `CryptoString(encrypted, decryptor)`), `setValue` / mutable
`value`, `length`-after-mutation, the `equals(String?)` & `equals(Any?)` overloads,
`compareTo`, `plus`, `get`, `subSequence`, `chars()` / `codePoints()`, and the member
`encrypt`. The **Kover floor is 95% (suite currently at 100%)** — add tests for every new
extension function or the gate fails. CI runs `koverVerifyDebug` on PRs into master.

### 0.4 🟢 Consumer impact is smaller than feared

A sweep of the testapp shows it only uses the single-arg `CryptoString("…")` constructor,
`.value`, and `.copy(...)` — **all of which survive** the redesign. So `UserEntity`,
`UserViewModel`, and the instrumented `AppDatabaseTest` need no changes. The breakage is
almost entirely inside the library's own unit tests (§0.3).

### 0.5 🟢 Minor staleness in the sections below

- The "before" `length` (§2.1) was `by this.value::length`; the stale-delegate bug that
  caused was **fixed 2026-06-08** — it is now `get() = value.length`. The "delegation
  boilerplate" framing is slightly overstated as a result.
- §5.1 cites Room KMP "as of 2.7"; the project is now on **Room 2.8.4**.
- The nullable-vs-empty semantics shift (§3.5 / §2.3) is a *separate* behavior change from
  the value-class swap: today `decrypt(null)` yields `CryptoString("")`, the plan returns
  `null`. Decide that deliberately, not as a side effect.

### 0.6 Revised recommendation

The §0.1 spike clears the only blocker: the value-class redesign **is technically viable**
on Room 2.8.4. Motivation has shifted, though — the wrapper is now small, 100%-covered, and
bug-free, so this is no longer pain-driven; the real prize is **KMP-readiness + API
simplicity**. Net: a sound, optional refactor. If/when it's picked up, the work is:

1. Land the value class + extension functions + (non-null) converter.
2. Migrate the unit suite (§0.3) and keep the 95% Kover gate green.
3. Use the **private-backing-property value class** (§0.7) so a forgotten converter is a
   compile error; keep a ciphertext round-trip test as belt-and-braces.
4. Decide the nullable-vs-empty semantics deliberately (§0.5).

Note `§2.1`'s proposed `value class CryptoString(val value: String)` should become
`value class CryptoString(private val raw: String) { val value get() = raw }` per §0.7.

### 0.7 ✅ SOLVED by spike: a private backing property makes it fail loud

The fail-silent footgun (§0.1 caveat) is caused by the `@JvmInline value class` wrapping a
*publicly visible* `String`: Room auto-unwraps it to a TEXT column, so a missing converter
stores plaintext with no error.

**Spike finding (2026-06-08): making the underlying property `private` defeats Room's native
unwrapping** — KSP then can't persist the field, so a missing `@TypeConverter` is a **compile
error**, while the class stays a zero-allocation value class with a normal String API. This is
the recommended shape:

```kotlin
@JvmInline
value class CryptoString(private val raw: String) {
    val value: String get() = raw
    companion object { val EMPTY = CryptoString("") }
}
```
- `CryptoString("secret")` and `.value` both work (public constructor + public getter).
- **Zero runtime overhead** — still inlines to `String`; the converter's non-null parameter
  stays unboxed. Only the *KSP-visible* property is private, which is enough for Room to
  decline auto-unwrapping.
- **Fail-loud** — forget `@TypeConverters(CryptoStringTypeConverter::class)` and KSP errors with
  *"Cannot figure out how to save this property into database"*, exactly like today's class.
- Proven to round-trip and store **ciphertext** when a converter *is* registered.

Spike matrix (Room 2.8.4 / KSP 2.3.9; no converter unless noted):

| Variant | Backing property | Converter | Result |
|---|---|---|---|
| A | `public val` | none | compiles → **fail-silent (plaintext)** ⚠️ |
| B | `private val` + private ctor | none | compile error → **fail-loud** ✅ |
| B + converter | `private val` | yes | compiles, round-trips, **stores ciphertext** ✅ |
| D | `private val` + public ctor | none | compile error → **fail-loud** ✅ |

Constructor visibility is irrelevant — the **private property** is the switch. Variant D is the
recommended form (cleanest API).

Options now superseded by the above:
- *Plain (non-value) class* — also fail-loud, but gives up zero-allocation for no gain over the
  private-property value class.
- *Custom Lint rule* — unnecessary; the compiler enforces it directly.
- *Wrapping a non-persistable type* — hacky; the private property achieves the same, cleanly.

This removes the only real objection to the redesign: we get value-class simplicity **and**
zero-overhead **and** the compile-time guarantee that you can't silently persist plaintext.

---

## 1. Background

### 1.1 What is a Value Class?

A **value class** (formerly inline class) is a Kotlin feature that wraps a single value with zero runtime overhead. At compile time, it provides type safety; at runtime, it's unwrapped to the underlying type.

```kotlin
@JvmInline
value class CryptoString(val value: String)

// At compile time: CryptoString and String are distinct types
// At runtime: CryptoString is just a String (no object allocation)
```

**Key benefits:**
- **Type safety** - Can't accidentally pass a plain `String` where `CryptoString` is expected
- **Zero overhead** - No object allocation, no wrapper at runtime
- **KMP compatible** - Works on all Kotlin targets

### 1.2 Current Implementation Issues

The current `CryptoString` wrapper class:
- Allocates an object for every encrypted field
- Implements `CharSequence` (rarely needed in practice)
- Has 99 lines of boilerplate for delegation
- Uses Android-specific APIs (`@RequiresApi`, `IntStream`)

---

## 2. Proposed Changes

### 2.1 New CryptoString (Value Class)

**Before (99 lines):**
```kotlin
class CryptoString(value: String? = null) : Comparable<CryptoString>, CharSequence {
    constructor(value: CryptoString?) : this(value?.value)
    constructor(encryptedValue: String, decryptor: Decryptor) : this(decryptor.decrypt(encryptedValue))

    var value: String = value ?: ""

    override val length: Int
        get() = value.length   // was `by this.value::length` — fixed 2026-06-08 (stale-delegate bug)
    // ... 80+ more lines of delegation and utilities
}
```

**After (15 lines):**
```kotlin
/**
 * Type-safe wrapper for strings that should be encrypted when stored in the database.
 *
 * This is a value class (inline class) which provides compile-time type safety
 * with zero runtime overhead - at runtime, this is just a String.
 *
 * The actual encryption/decryption happens in [CryptoStringTypeConverter].
 */
@JvmInline
value class CryptoString(val value: String) {
    override fun toString(): String = value

    companion object {
        val EMPTY = CryptoString("")
    }
}
```

### 2.2 Extension Functions (Optional Utilities)

```kotlin
// CryptoStringExtensions.kt

/**
 * Encrypts this CryptoString using the provided encryptor.
 * @return The encrypted string (ciphertext).
 */
fun CryptoString.encrypt(encryptor: Encryptor): String =
    encryptor.encrypt(value)

/**
 * Converts a plain String to a CryptoString.
 */
fun String.toCryptoString(): CryptoString =
    CryptoString(this)

/**
 * Decrypts this encrypted String and wraps it in a CryptoString.
 */
fun String.decryptToCryptoString(decryptor: Decryptor): CryptoString =
    CryptoString(decryptor.decrypt(this))

/**
 * Compares this CryptoString with another.
 */
operator fun CryptoString.compareTo(other: CryptoString): Int =
    value.compareTo(other.value)

/**
 * Compares this CryptoString with a plain String.
 */
fun CryptoString.contentEquals(other: String): Boolean =
    value == other

/**
 * Returns the length of the underlying string.
 */
val CryptoString.length: Int get() = value.length

/**
 * Returns true if the underlying string is empty.
 */
fun CryptoString.isEmpty(): Boolean = value.isEmpty()

/**
 * Returns true if the underlying string is not empty.
 */
fun CryptoString.isNotEmpty(): Boolean = value.isNotEmpty()

/**
 * Returns true if the underlying string is blank.
 */
fun CryptoString.isBlank(): Boolean = value.isBlank()

/**
 * Returns true if the underlying string is not blank.
 */
fun CryptoString.isNotBlank(): Boolean = value.isNotBlank()
```

### 2.3 Updated TypeConverter

```kotlin
/**
 * Room TypeConverter for [CryptoString].
 *
 * - When writing to DB: Encrypts the plain value
 * - When reading from DB: Decrypts the stored value
 */
class CryptoStringTypeConverter {

    @TypeConverter
    fun fromCryptoString(crypto: CryptoString?): String? {
        if (crypto == null) return null
        return crypto.encrypt(CryptoRoomDatabase.getEncryptor())
    }

    @TypeConverter
    fun toCryptoString(encrypted: String?): CryptoString? {
        if (encrypted == null) return null
        return encrypted.decryptToCryptoString(CryptoRoomDatabase.getDecryptor())
    }
}
```

### 2.4 CryptoRoomDatabase (Simplified)

```kotlin
/**
 * Base class for Room databases with field-level encryption support.
 *
 * Usage:
 * 1. Extend this class for your database
 * 2. Call [setCryptoHelpers] during initialization
 * 3. Use [CryptoString] for fields that should be encrypted
 * 4. Include [CryptoStringTypeConverter] in your @TypeConverters
 */
@TypeConverters(CryptoStringTypeConverter::class)
abstract class CryptoRoomDatabase : RoomDatabase() {

    /**
     * Initialize the encryption/decryption helpers.
     * Must be called before any database operations.
     */
    fun setCryptoHelpers(encryptor: Encryptor, decryptor: Decryptor) {
        Companion.encryptor = encryptor
        Companion.decryptor = decryptor
    }

    companion object {
        private var encryptor: Encryptor? = null
        private var decryptor: Decryptor? = null

        internal fun getEncryptor(): Encryptor =
            encryptor ?: throw EncryptorNotInitializedException()

        internal fun getDecryptor(): Decryptor =
            decryptor ?: throw DecryptorNotInitializedException()
    }
}
```

---

## 3. Migration Guide

### 3.1 Entity Changes

**No changes required** - entities continue to use `CryptoString`:

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,                    // Not encrypted
    val email: CryptoString,             // Encrypted
    val socialSecurityNumber: CryptoString,  // Encrypted
)
```

### 3.2 Creating CryptoString Values

**Before:**
```kotlin
// Multiple ways to create
val crypto1 = CryptoString("secret")
val crypto2 = CryptoString(otherCryptoString)
val crypto3 = CryptoString(encryptedValue, decryptor)
```

**After:**
```kotlin
// Single constructor
val crypto1 = CryptoString("secret")
val crypto2 = "secret".toCryptoString()

// Copy is just assignment (value class is immutable)
val crypto3 = crypto1

// From encrypted value (rare - usually TypeConverter handles this)
val crypto4 = encryptedValue.decryptToCryptoString(decryptor)
```

### 3.3 Accessing the Value

**Before:**
```kotlin
val plain: String = cryptoString.value
val plain2: String = cryptoString.toString()
val length: Int = cryptoString.length  // CharSequence delegation
```

**After:**
```kotlin
val plain: String = cryptoString.value
val plain2: String = cryptoString.toString()
val length: Int = cryptoString.length  // Extension property
```

### 3.4 Comparisons

**Before:**
```kotlin
if (cryptoString == otherCrypto) { }
if (cryptoString.equals("plaintext")) { }
```

**After:**
```kotlin
if (cryptoString == otherCrypto) { }  // Still works (value equality)
if (cryptoString.contentEquals("plaintext")) { }  // Extension function
// Or simply:
if (cryptoString.value == "plaintext") { }
```

### 3.5 Nullable Handling

**Before:**
```kotlin
val crypto: CryptoString? = null
val safe = crypto ?: CryptoString()  // Empty default
```

**After:**
```kotlin
val crypto: CryptoString? = null
val safe = crypto ?: CryptoString.EMPTY  // Companion object constant
// Or:
val safe = crypto ?: CryptoString("")
```

---

## 4. What's Lost (And Why It's OK)

### 4.1 CharSequence Implementation

**Lost:** Can't pass `CryptoString` directly where `CharSequence` is expected.

**Mitigation:** Use `.value` to get the underlying String (which is a CharSequence):
```kotlin
textView.text = cryptoString.value  // Instead of cryptoString
```

**Why it's OK:** In practice, you almost always need the String anyway. The `CharSequence` implementation was rarely used directly.

### 4.2 Mutable `value` Property

**Lost:** Can't modify the value in place:
```kotlin
// Before: cryptoString.value = "new value"
```

**Why it's OK:** Immutability is better. Create a new instance:
```kotlin
// After: cryptoString = CryptoString("new value")
// Or: cryptoString = "new value".toCryptoString()
```

### 4.3 Copy Constructor

**Lost:** `CryptoString(otherCryptoString)`

**Why it's OK:** Value classes are immutable and inlined. Just assign:
```kotlin
val copy = original  // No object to copy - it's just a String at runtime
```

### 4.4 IntStream Methods (chars(), codePoints())

**Lost:** `@RequiresApi` methods for Java 8 streams.

**Why it's OK:** These were Android-specific and rarely used. Access via `.value` if needed:
```kotlin
cryptoString.value.chars()  // If you really need it
```

---

## 5. KMP Considerations

### 5.1 Room KMP Compatibility

Room supports KMP (since 2.7; the project is currently on **2.8.4**). The value class approach is fully compatible:

```kotlin
// commonMain - works on all platforms
@JvmInline
value class CryptoString(val value: String)

// Room TypeConverter works the same way
// Encryption implementation can be platform-specific via expect/actual
```

### 5.2 Platform-Specific Encryption

```kotlin
// commonMain
expect fun createEncryptor(key: ByteArray): Encryptor
expect fun createDecryptor(key: ByteArray): Decryptor

// androidMain - Android Keystore
actual fun createEncryptor(key: ByteArray): Encryptor =
    AndroidKeystoreEncryptor(key)

// iosMain - Keychain + CommonCrypto
actual fun createEncryptor(key: ByteArray): Encryptor =
    IOSKeychainEncryptor(key)

// jvmMain - BouncyCastle or Java Crypto
actual fun createEncryptor(key: ByteArray): Encryptor =
    JvmEncryptor(key)
```

### 5.3 Future KMP Migration Path

1. **Phase 1 (Now):** Simplify to value class (Android-only)
2. **Phase 2 (Later):** Move to KMP structure with expect/actual for encryption
3. **Phase 3 (Later):** Add platform-specific encryption implementations

---

## 6. File Structure (After Redesign)

```
CryptoRoomDB/src/main/java/com/duck/cryptoroomdb/
├── CryptoRoomDatabase.kt           # Base database class (simplified)
├── types/
│   ├── CryptoString.kt             # Value class (15 lines)
│   └── CryptoStringExtensions.kt   # Extension functions (optional)
├── typeconverter/
│   └── CryptoStringTypeConverter.kt  # Room TypeConverter
├── interfaces/
│   ├── Encryptor.kt                # Encryption interface (unchanged)
│   └── Decryptor.kt                # Decryption interface (unchanged)
└── exceptions/
    ├── EncryptorNotInitializedException.kt
    └── DecryptorNotInitializedException.kt
```

---

## 7. Testing

> **⚠️ Out of date — see §0.3.** A real test suite now exists (`CryptoStringTest`,
> `CryptoStringTypeConverterTest`, `CryptoStringRoundTripTest`, `CryptoRoomDatabaseTest`,
> plus `FakeCryptor`/`TestCryptoDatabase`/`TestEntity`/`TestDao`/`CryptoTestSupport` fixtures).
> These run on the JVM under Robolectric (`@RunWith(AndroidJUnit4::class)` + `runBlocking`,
> not `runTest`; `db.testDao` is a property, not `testDao()`). The sketches below must be
> reconciled with the existing suite and the 95% Kover floor — they are illustrative only.

### 7.1 Value Class Behavior

```kotlin
class CryptoStringTest {

    @Test
    fun `value class provides type safety`() {
        val crypto: CryptoString = CryptoString("secret")
        val plain: String = "secret"

        // These are different types at compile time
        // crypto == plain  // Won't compile!

        // Must explicitly compare values
        assertEquals(crypto.value, plain)
        assertTrue(crypto.contentEquals(plain))
    }

    @Test
    fun `value class has no runtime overhead`() {
        // At runtime, CryptoString is just a String
        // No object allocation occurs
        val crypto = CryptoString("test")
        assertEquals("test", crypto.value)
    }

    @Test
    fun `empty constant works`() {
        val empty = CryptoString.EMPTY
        assertTrue(empty.isEmpty())
        assertEquals("", empty.value)
    }
}
```

### 7.2 TypeConverter Integration

```kotlin
@RunWith(AndroidJUnit4::class)
class CryptoStringTypeConverterTest {

    private lateinit var db: TestDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, TestDatabase::class.java)
            .build()
        db.setCryptoHelpers(TestEncryptor(), TestDecryptor())
    }

    @Test
    fun `encrypted field round-trips correctly`() = runTest {
        val original = CryptoString("sensitive data")
        val entity = TestEntity(id = "1", secret = original)

        db.testDao().insert(entity)
        val retrieved = db.testDao().getById("1")

        assertEquals(original, retrieved.secret)
        assertEquals("sensitive data", retrieved.secret.value)
    }

    @Test
    fun `null encrypted field works`() = runTest {
        val entity = TestEntity(id = "1", secret = null)

        db.testDao().insert(entity)
        val retrieved = db.testDao().getById("1")

        assertNull(retrieved.secret)
    }
}
```

---

## 8. Summary

| Aspect | Before (Wrapper) | After (Value Class) |
|--------|------------------|---------------------|
| Lines of code | 99 | 15 |
| Runtime overhead | Object allocation | None |
| Type safety | Yes | Yes |
| CharSequence | Yes | No (use `.value`) |
| Mutable | Yes | No (immutable) |
| KMP ready | No (IntStream) | Yes |
| Room compatible | Yes | Yes |

**Recommendation:** Proceed with the value class redesign. The benefits (simplicity, performance, KMP readiness) outweigh the minor inconveniences (accessing `.value` for CharSequence contexts).

---

## Changelog

| Date | Change |
|------|--------|
| 2026-01-18 | Initial design document created |
| 2026-06-08 | Reviewed against current codebase (tests, Kover gate, CI, AGP 9 / Room 2.8.4). Added §0: critical Room native value-class-unwrap risk (could bypass the converter and store plaintext), TypeConverter name-mangling caveat, test-suite migration scope, and minor staleness fixes. Recommend a round-trip spike before proceeding. |
| 2026-06-08 | Ran the §0.1/§0.2 spike (throwaway value class + converter + @Database in test sources). Result: Room 2.8.4 invokes the @TypeConverter and stores ciphertext (no plaintext leak); non-null value-class converter signature compiled fine (no mangling issue). Redesign marked technically viable. Exposed a new fail-silent caveat: omitting the converter would store plaintext rather than erroring. |
| 2026-06-08 | Spiked the fail-silent caveat (§0.7, four variants). Solved: a `private` backing property makes Room decline native unwrapping, so a missing converter is a compile error — while keeping zero-overhead + a String API. Recommended shape is now `value class CryptoString(private val raw: String) { val value get() = raw }`. This removes the last objection to the redesign. |
