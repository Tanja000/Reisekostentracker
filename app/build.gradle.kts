plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "de.saveiq.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "de.saveiq.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            // Debug build
        }
        release {
            isMinifyEnabled = false
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

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // MapLibre Android SDK (OpenFreeMap vector tiles, 0€, license free)
  implementation("org.maplibre.gl:android-sdk:11.8.2")
}

abstract class CopySaveIqArtifactsTask : DefaultTask() {
    init {
        doNotTrackState("Copies final artifacts directly to root project directory")
    }

    @get:Internal
    abstract val debugDir: DirectoryProperty

    @get:Internal
    abstract val bundleDir: DirectoryProperty

    @get:Internal
    abstract val rootOutputDir: DirectoryProperty

    @TaskAction
    fun copyArtifacts() {
        val rootDirFile = rootOutputDir.get().asFile
        val debugDirFile = debugDir.get().asFile
        val bundleDirFile = bundleDir.get().asFile

        val srcApk = File(debugDirFile, "app-debug.apk")
        if (srcApk.exists()) {
            val rootApk = File(rootDirFile, "SaveIQ.apk")
            srcApk.copyTo(rootApk, overwrite = true)
            srcApk.copyTo(File(debugDirFile, "SaveIQ.apk"), overwrite = true)
            println("Created APK in root: ${rootApk.absolutePath}")
        }

        val bundleFile = File(bundleDirFile, "app-debug.aab")
        if (bundleFile.exists()) {
            val rootAab = File(rootDirFile, "SaveIQ.aab")
            val rootApp = File(rootDirFile, "SaveIQ.app")
            bundleFile.copyTo(rootAab, overwrite = true)
            bundleFile.copyTo(rootApp, overwrite = true)
            println("Created Google Play Bundle in root: ${rootAab.absolutePath} and ${rootApp.absolutePath}")
        }

        // Delete legacy TrackMyCent.apk if exists
        val legacyRoot = File(rootDirFile, "TrackMyCent.apk")
        if (legacyRoot.exists()) {
            legacyRoot.delete()
        }
        val legacyInApk = File(debugDirFile, "TrackMyCent.apk")
        if (legacyInApk.exists()) {
            legacyInApk.delete()
        }
    }
}

tasks.register<CopySaveIqArtifactsTask>("copySaveIqArtifacts") {
    debugDir.set(layout.buildDirectory.dir("outputs/apk/debug"))
    bundleDir.set(layout.buildDirectory.dir("outputs/bundle/debug"))
    rootOutputDir.set(project.rootDir)
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    dependsOn("bundleDebug")
    finalizedBy("copySaveIqArtifacts")
}

tasks.matching { it.name == "bundleDebug" }.configureEach {
    finalizedBy("copySaveIqArtifacts")
}


