import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinKSP)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kover)
    `maven-publish`
}

group = "com.github.projectdelta6"

configure<LibraryExtension> {
    namespace = "com.duck.cryptoroomdb"
    compileSdk = libs.versions.compileSdk.get().toInt()

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    defaultConfig {
        minSdk = libs.versions.baseRepoMinSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests {
            // Lets Robolectric load the merged manifest/resources on the JVM unit-test
            // classpath so RoomDatabase + the in-memory SQLite round-trip run without an
            // emulator. All library tests run as plain `testDebugUnitTest`.
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

kover {
    reports {
        // Coverage gate: `./gradlew :CryptoRoomDB:koverVerifyDebug` fails below this floor.
        // Measures only the published library surface (com.duck.cryptoroomdb.*); the
        // Robolectric test fixtures (test-only @Database/entity/dao) and Room-generated
        // *_Impl classes are excluded. The suite currently covers 100% of that surface;
        // the floor sits just below to catch regressions without tripping on a minor
        // refactor before its test lands.
        verify {
            rule {
                minBound(95)
            }
        }
        filters {
            includes {
                classes("com.duck.cryptoroomdb.*")
            }
            excludes {
                // Test-only Room fixtures live under .roomtest; never published.
                classes("com.duck.cryptoroomdb.roomtest.*")
                // Room-generated implementations.
                classes("*_Impl")
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.kotlin.stdlib)

    // Room library
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Unit testing — Robolectric runs the Room round-trip on the JVM (no emulator).
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.robolectric)
    // Annotation-process the test-only @Database defined in src/test (com.duck.cryptoroomdb.roomtest).
    kspTest(libs.androidx.room.compiler)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = "com.github.projectdelta6"
            artifactId = project.name
            version = libs.versions.room.get()
        }
    }
}
