# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CryptoRoomDB is an Android library that extends Room to provide field-level encryption/decryption for database entities. The library version is kept in sync with the Room version it targets.

## Build Commands

```bash
# Build the library
./gradlew :CryptoRoomDB:assembleRelease

# Build the test app
./gradlew :testapp:assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew :testapp:connectedAndroidTest --tests "com.duck.cryptoroomdbtestapp.data.db.AppDatabaseTest"

# Clean build
./gradlew clean
```

## Architecture

### Library Module (`CryptoRoomDB/`)
Package: `com.duck.cryptoroomdb`

Core components:
- **`CryptoRoomDatabase`** - Abstract base class extending `RoomDatabase`. Manages static `Encryptor`/`Decryptor` instances via `setCryptoHelpers()`. Consumer databases extend this class.
- **`CryptoString`** - `@JvmInline value class` wrapping a **private** `String` (read via `.value`). Holds decrypted values in app code; encryption only happens during DB write via TypeConverter. The backing property is private on purpose: it stops Room from natively unwrapping the value class to a TEXT column (which would bypass the converter and store plaintext), so the converter is mandatory and its absence is a compile error. Utility operations (`encrypt`, `toCryptoString`, `length`, `isEmpty`, `compareTo`, etc.) are extension functions in `CryptoStringExtensions.kt`.
- **`CryptoStringTypeConverter`** - Room TypeConverter that encrypts on write (`fromCryptoString`: `CryptoString` -> `String`) and decrypts on read (`toCryptoString`: `String` -> `CryptoString`).
- **`Encryptor`/`Decryptor`** interfaces - Consumer implements these with their encryption logic. Often a single class implements both.

### Test App Module (`testapp/`)
Package: `com.duck.cryptoroomdbtestapp`

Demonstrates library usage with Jetpack Compose UI. Shows:
- `AppDataBase` extending `CryptoRoomDatabase`
- `Cryptor` class implementing both `Encryptor` and `Decryptor`
- Entity with `CryptoString` field (`UserEntity.secret`)
- Raw query to display encrypted values stored in DB

## Key Implementation Pattern

```kotlin
// 1. Database extends CryptoRoomDatabase
@Database(entities = [UserEntity::class], version = 1)
@TypeConverters(CryptoStringTypeConverter::class)
abstract class AppDatabase : CryptoRoomDatabase()

// 2. Initialize with crypto helpers after Room.databaseBuilder()
database.setCryptoHelpers(cryptor, cryptor)

// 3. Use CryptoString in entities for encrypted fields
@Entity
data class UserEntity(
    val name: String,           // stored as plain text
    val secret: CryptoString    // encrypted in DB, decrypted in code
)
```

## Version Management

- Library version is `<room>.<roomLibRevision>` — the targeted Room version plus an independent
  library-release revision (e.g. `2.8.4.1`). Both are in `libs.versions.toml`. Bump
  `roomLibRevision` for library-only releases; reset it to `1` when `room` is bumped. (A 4th
  numeric segment is used, not a `-N` suffix, so versions sort *after* the bare Room version.)
- JitPack publishing configured in `CryptoRoomDB/build.gradle.kts`; a release is a git tag matching
  the version (e.g. `2.8.4.1`).
- Test app versionName uses the same `<room>.<roomLibRevision>` string for consistency.
