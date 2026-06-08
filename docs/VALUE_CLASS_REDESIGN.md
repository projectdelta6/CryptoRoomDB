# ADR: `CryptoString` as a value class

**Status:** Accepted & implemented (2026-06-08). Original proposal 2026-01-18; reviewed, spiked,
and implemented since — see [History](#history).

> This started as a longer design proposal. Once implemented it was trimmed to this ADR; the
> original proposal body (with pre-implementation code sketches) remains in git history.

## Context

`CryptoString` was a ~99-line class implementing `CharSequence` + `Comparable`, with several
constructors, a mutable `value`, and Android-only APIs (`IntStream`, `@RequiresApi`). The goals of
the redesign were a **simpler API** and **KMP-readiness**. By the time it was picked up the library
also had a full test suite, a 95% Kover gate, and CI.

The central risk: Room 2.6+ **natively unwraps `@JvmInline value class` columns** to their
underlying type. A `CryptoString` wrapping a `String` could therefore be mapped straight to a `TEXT`
column and **bypass `CryptoStringTypeConverter`, silently storing plaintext** — the opposite of the
library's purpose.

## Decision

`CryptoString` is a value class with a **private** backing property:

```kotlin
@JvmInline
value class CryptoString(private val raw: String) {
    val value: String get() = raw
    companion object { val EMPTY = CryptoString("") }
}
```

- Utilities are **extension functions** (`CryptoStringExtensions.kt`): `encrypt`, `toCryptoString`,
  `decryptToCryptoString`, `compareTo`, `contentEquals`, `length`, `isEmpty`/`isNotEmpty`/
  `isBlank`/`isNotBlank`.
- The converter is **non-null**: `fromCryptoString` / `toCryptoString`.
- `getEncryptor`/`getDecryptor` are `internal`.

## Why the private backing property (the load-bearing choice)

The `private` property is what stops Room's native unwrapping, so the `@TypeConverter` becomes
**mandatory — forgetting it is a compile error, not a silent plaintext leak**. Proven by spike
(Room 2.8.4 / KSP 2.3.9; no converter unless noted):

| Variant | Backing property | Converter | Result |
|---|---|---|---|
| A | `public val` | none | compiles → **fail-silent (plaintext)** ⚠️ |
| B | `private val` + private ctor | none | compile error → **fail-loud** ✅ |
| B + converter | `private val` | yes | compiles, round-trips, **stores ciphertext** ✅ |
| D | `private val` + public ctor | none | compile error → **fail-loud** ✅ |

Constructor visibility is irrelevant — the private property is the switch (so the public
constructor + `CryptoString("x")` ergonomics are kept). Zero overhead is retained (it still inlines
to `String`; the non-null converter parameter stays unboxed). The non-null converter signature
compiled cleanly — **no value-class name-mangling problem** on this toolchain. A plain class or a
custom Lint rule were considered and rejected: the private-property value class gives fail-loud
*and* zero-overhead with no extra machinery.

## Consequences

- **Breaking API change.** Removed: `CharSequence`, `Comparable`, the secondary constructors
  (no-arg, copy, decryptor), mutable `value`/`setValue`, `plus`, `get`, `subSequence`,
  `chars`/`codePoints`, and the member `encrypt`. Most live on as extensions; read via `.value`.
  `CryptoString` is now immutable — "update" by replacing it (`entity.copy(secret = CryptoString(…))`).
- **Consumer impact is small.** The testapp used only `CryptoString("…")`, `.value`, and `.copy(...)`
  — all of which survive — so it needed no changes. Breakage was confined to the library's own unit
  tests, which were migrated. **100% coverage retained**, 95% gate green.
- **Semantics:** chose non-null `CryptoString` + non-null converter (matches real usage and the
  proven spike). Nullable encrypted fields would need their own nullable converter (untested) — out
  of scope for now.
- **Base-class `@TypeConverters` removed.** Room does not inherit `@TypeConverters` from a
  `@Database`'s superclass (verified by spike), so the annotation on `CryptoRoomDatabase` was dead
  and misleading. Consumers register the converter on their own `@Database` — which a `CryptoString`
  field won't compile without anyway.

## Outstanding

- **Automated regression guard deferred.** The fail-loud invariant (private backing property) is not
  yet guarded by a test. A true negative-compilation test via `kotlin-compile-testing` (kctfork
  0.7.1) is blocked: kctfork bundles Kotlin/KSP **2.1.10**, which crashes running Room 2.8.4's
  processor under Kotlin 2.4 / KSP 2.3.9 (KSP2 NPEs in the engine; KSP1 crashes the bundled FIR
  compiler). Revisit when kctfork ships a Kotlin-2.4 / KSP-2.3.x-compatible release. Until then the
  invariant rests on the KDoc on `CryptoString` and the spike evidence above.

## History

| Date | Event |
|------|-------|
| 2026-01-18 | Original proposal authored (full design preserved in git history). |
| 2026-06-08 | Reviewed against the current codebase. Spiked Room's value-class behaviour: a registered `@TypeConverter` is used and stores ciphertext (no plaintext leak); a non-null value-class converter signature compiles (no mangling issue); a **private** backing property makes a missing converter a compile error; Room does not inherit `@TypeConverters` from a superclass. |
| 2026-06-08 | Implemented: value class + extensions + non-null converter + `internal` getters; unit suite migrated (100% coverage); decorative base-class `@TypeConverters` removed; README/KDoc updated. Negative-compilation guard deferred (kctfork/Kotlin-2.4 incompatibility). |
