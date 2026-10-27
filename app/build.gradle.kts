plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "de.saveiq.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.saveiq"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.2"
    }

    signingConfigs {
        create("release") {
            storeFile = file("saveiq-release-key.jks")
            storePassword = "SaveIQ2026!"
            keyAlias = "saveiq"
            keyPassword = "SaveIQ2026!"
        }
    }

    buildTypes {
        debug {
            // Debug build
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        aidl = false
        buildConfig = false
        shaders = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Arch Components
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    // Tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Instrumented tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Local tests: jUnit, coroutines, Android runner
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumented tests: jUnit rules and runners
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)

    // MapLibre Android SDK (OpenFreeMap vector tiles, 0€, license free)
    implementation("org.maplibre.gl:android-sdk:11.8.2")
}

abstract class CopySaveIqArtifactsTask : DefaultTask() {
    init {
        doNotTrackState("Copies final release-signed AAB and APK directly to root project directory")
    }

    @get:Internal
    abstract val debugDir: DirectoryProperty

    @get:Internal
    abstract val releaseBundleDir: DirectoryProperty

    @get:Internal
    abstract val rootOutputDir: DirectoryProperty

    @TaskAction
    fun copyArtifacts() {
        val rootDirFile = rootOutputDir.get().asFile
        val debugDirFile = debugDir.get().asFile
        val releaseBundleDirFile = releaseBundleDir.get().asFile

        val srcApk = File(debugDirFile, "app-debug.apk")
        if (srcApk.exists()) {
            val rootApk = File(rootDirFile, "SaveIQ.apk")
            srcApk.copyTo(rootApk, overwrite = true)
            srcApk.delete()
            println("Created APK in root: ${rootApk.absolutePath} (removed app-debug.apk)")
        }
        val duplicateApkInBuild = File(debugDirFile, "SaveIQ.apk")
        if (duplicateApkInBuild.exists()) {
            duplicateApkInBuild.delete()
        }

        val releaseBundleFile = File(releaseBundleDirFile, "app-release.aab")
        if (releaseBundleFile.exists()) {
            val rootAab = File(rootDirFile, "SaveIQ.aab")
            val rootApp = File(rootDirFile, "SaveIQ.app")
            releaseBundleFile.copyTo(rootAab, overwrite = true)
            releaseBundleFile.copyTo(rootApp, overwrite = true)
            println("Created Release-Signed Google Play Bundle in root: ${rootAab.absolutePath} and ${rootApp.absolutePath}")
        }

        // Remove any legacy or stray files in root
        listOf("TrackMyCent.apk", "app-debug.apk").forEach { name ->
            val f = File(rootDirFile, name)
            if (f.exists()) f.delete()
        }
    }
}

tasks.register<CopySaveIqArtifactsTask>("copySaveIqArtifacts") {
    debugDir.set(layout.buildDirectory.dir("outputs/apk/debug"))
    releaseBundleDir.set(layout.buildDirectory.dir("outputs/bundle/release"))
    rootOutputDir.set(project.rootDir)
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    dependsOn("bundleRelease")
    finalizedBy("copySaveIqArtifacts")
}

tasks.matching { it.name == "bundleRelease" }.configureEach {
    finalizedBy("copySaveIqArtifacts")
}
