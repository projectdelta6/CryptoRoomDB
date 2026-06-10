# === CryptoRoomDB consumer rules ===
# Shipped in the AAR via consumerProguardFiles; applied automatically in consuming apps.
#
# Note: encryption here is TypeConverter-based, NOT reflection-based. Entity field-name
# obfuscation is governed by the consumer's own Room keeps, so there is no extra
# obfuscation hazard from the encryption layer — these rules only protect CryptoRoomDB's
# own R8-sensitive surface.

# Room instantiates the TypeConverter by no-arg constructor in the generated *_Impl
# (`CryptoStringTypeConverter()`), references it as a KClass in
# getRequiredTypeConverterClasses(), and calls the @TypeConverter methods directly.
-keep class com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter { <init>(); }
-keepclassmembers class com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter {
    @androidx.room.TypeConverter <methods>;
}

# Persisted value type appearing in generated DAO _Impl signatures.
-keep class com.duck.cryptoroomdb.types.CryptoString { *; }

# Crypto base RoomDatabase reached via the consumer's @Database subclass + its _Impl.
-keep class com.duck.cryptoroomdb.CryptoRoomDatabase { *; }

# Encryptor/Decryptor deliberately omitted — consumer-implemented and called virtually,
# no keep needed.
