import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
	if (keystorePropertiesFile.isFile) {
		keystorePropertiesFile.inputStream().use(::load)
	}
}

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_21
	}
}

android {
	namespace = "io.github.melastore.stanza"
	compileSdk = 37

	defaultConfig {
		applicationId = "io.github.melastore.stanza"
		minSdk = 31
		targetSdk = 37
		versionName = "0.1.2"
		versionCode = 3
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	if (keystorePropertiesFile.isFile) {
		signingConfigs {
			create("release") {
				storeFile = rootProject.file(keystoreProperties.required("storeFile"))
				storePassword = keystoreProperties.required("storePassword")
				keyAlias = keystoreProperties.required("keyAlias")
				keyPassword = keystoreProperties.required("keyPassword")
			}
		}
	}

	buildTypes {
		release {
			signingConfigs.findByName("release")?.let { signingConfig = it }
			// AGP otherwise writes the git HEAD into the APK, which makes the bytes depend on the
			// checked-out commit and breaks reproducible-build verification.
			vcsInfo { include = false }
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
		}
	}

	buildFeatures {
		compose = true
	}

	testOptions {
		unitTests.isIncludeAndroidResources = true
		unitTests.all {
			it.maxHeapSize = "2048m"
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}

	sourceSets {
		named("main") {
			kotlin.directories.add("src/main/kotlin")
		}
		named("test") {
			kotlin.directories.add("src/test/kotlin")
		}
	}

	dependenciesInfo {
		includeInApk = false
		includeInBundle = false
	}
}

dependencies {
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.compose.material.icons.core)
	implementation(libs.androidx.compose.material.icons.extended)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.compose)
	implementation(libs.androidx.lifecycle.viewmodel.compose)
	implementation(libs.androidx.datastore.preferences)
	implementation(libs.androidx.glance)
	implementation(libs.androidx.glance.appwidget)
	implementation(libs.dev.chrisbanes.haze)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.coroutines.core)

	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)

	testImplementation(libs.junit)
	testImplementation(libs.kotlinx.coroutines.test)
	testImplementation(libs.androidx.test.core)
	testImplementation(libs.robolectric)
	testImplementation(libs.robolectric.android)
	testImplementation(libs.androidx.test.ext.junit)
}

fun Properties.required(name: String): String = getProperty(name)?.takeIf { it.isNotBlank() }
	?: error("keystore.properties is missing a value for '$name'")
