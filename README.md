# CryptoRoomDB

CryptoRoomDB is an Android library that extends Room to provide field-level encryption and
decryption for your database entities. It allows you to store sensitive data securely in your SQLite
database, with minimal changes to your existing Room setup.

[![](https://jitpack.io/v/projectdelta6/CryptoRoomDB.svg)](https://jitpack.io/#projectdelta6/CryptoRoomDB)

## Features

- Field-level encryption and decryption for Room entities
- Easy integration with existing Room databases
- Customizable encryption logic
- Compatible with Room's Flow and LiveData

## Setup

### 1. Add the JitPack repository

Add the following to your root `build.gradle`:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Add the dependency

Add the following to your app/module `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.projectdelta6:CryptoRoomDB:{version}' // Use a version matching your Room version
}
```

### 3. Configure your Room database

- Extend `CryptoRoomDatabase` instead of `RoomDatabase`.
- Add the `CryptoStringTypeConverter` to your database using `@TypeConverters`.
- Set your encryptor and decryptor helpers using `setCryptoHelpers()`.

Example:

```kotlin
@Database(entities = [UserEntity::class], version = 1)
@TypeConverters(CryptoStringTypeConverter::class)
abstract class AppDatabase : CryptoRoomDatabase() {
    abstract val userDao: UserDao

    companion object {
        fun getInstance(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "db-name")
                .build().apply {
                    val cryptor = Cryptor(context)
                    setCryptoHelpers(cryptor, cryptor)
                }
    }
}
```

### 4. Use Crypto types in your entities

Use the provided `CryptoString` (or your own type) for fields you want encrypted:

```kotlin
@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val secret: CryptoString
)
```

## CryptoString Type

The `CryptoString` type is a `@JvmInline value class` wrapping a `String`. **It only ever holds the
decrypted (plain) value in your app code** — the value is encrypted only when written to the
database and decrypted automatically when read back. Being a value class, it adds no runtime
overhead (at runtime it is just the `String`) and is immutable.

Access the plain value via `.value`; `toString()` returns it too:

```kotlin
val secret = CryptoString("mySecret")
val plain: String = secret.value   // the decrypted value
println(secret)                    // prints: mySecret

// Immutable, with value-based equality:
if (secret == CryptoString("mySecret")) { /* ... */ }   // true
if (secret.value == "mySecret") { /* ... */ }           // compare against a plain String
```

Helper extension functions live alongside it: `String.toCryptoString()`, `contentEquals(String)`,
`length`, `isEmpty`/`isNotEmpty`/`isBlank`/`isNotBlank`, and `compareTo`.

When used with CryptoRoomDB and the provided TypeConverter, values are automatically encrypted when
written to the database and decrypted when read. **At no point does the `CryptoString` instance
itself hold the encrypted value in your app code.**

> **Registering `CryptoStringTypeConverter` is mandatory.** `CryptoString` deliberately hides its
> backing value, so Room cannot persist the field on its own — if you forget
> `@TypeConverters(CryptoStringTypeConverter::class)` on your `@Database`, the build fails rather
> than silently storing plaintext.

> **Migrating from earlier versions:** `CryptoString` is now a value class. The old
> `CharSequence` behaviour, in-place mutation (`cryptoString.value = …` / `setValue`), the extra
> constructors (`CryptoString()`, copy, and decryptor constructors), the `+` operator, and
> `cryptoString == "plainString"` have been removed. Use `.value` (e.g.
> `cryptoString.value == "plainString"`), `String.toCryptoString()`, `copy(secret = CryptoString(…))`
> on your entity, or the new extension helpers. Entity fields and the `setCryptoHelpers(...)` setup
> are unchanged.

## Usage Example

Insert and retrieve encrypted data as usual with Room:

```kotlin
val user = UserEntity(name = "Alice", secret = CryptoString("mySecret"))
db.userDao.upsert(user)
val loaded = db.userDao.getUser(user.id)
println(loaded.secret) // prints: mySecret
```

## Demo App

A sample app is included in the `testapp/` module. It demonstrates:

- Adding, editing, and deleting users
- Displaying both decrypted and encrypted values
- Real-time updates using Compose and Flow

## License

MIT
