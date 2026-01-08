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
- **`CryptoString`** - String wrapper implementing `CharSequence`. Holds decrypted values in app code; encryption only happens during DB write via TypeConverter.
- **`CryptoStringTypeConverter`** - Room TypeConverter that encrypts on write (`CryptoString` -> `String`) and decrypts on read (`String` -> `CryptoString`).
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

- Library version matches Room version (see `libs.versions.toml`)
- JitPack publishing configured in `CryptoRoomDB/build.gradle.kts`
- Test app versionName also uses Room version for consistency
