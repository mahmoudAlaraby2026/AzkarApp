import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    androidLibrary {
       namespace = "com.mahmoud.azkarapp.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain {
            resources.srcDirs(layout.buildDirectory.dir("generated/compose/packagedResources"))
            dependencies {
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.work.runtime.ktx)
            }
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material)
            implementation(libs.compose.materialIcons)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kmp.player)
            implementation(libs.coil.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    packageOfResClass = "azkarapp.shared.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

tasks.matching { it.name.startsWith("generateComposeResClass") }.configureEach {
    val buildDir = layout.buildDirectory.dir("generated/compose/resourceGenerator")
    doLast {
        val generatedDir = buildDir.get().asFile
        if (generatedDir.exists()) {
            generatedDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                var content = file.readText()
                content = content.replace(Regex("@delegate:ResourceContentHash\\(.*?\\)[\\r\\n]*"), "")
                content = content.replace(Regex("import org\\.jetbrains\\.compose\\.resources\\.ResourceContentHash[\\r\\n]*"), "")
                file.writeText(content)
            }
        }
    }
}

val copyComposeResourcesWithPackage = tasks.register("copyComposeResourcesWithPackage", Copy::class) {
    dependsOn(tasks.matching { it.name.startsWith("prepareComposeResources") })
    into(layout.buildDirectory.dir("generated/compose/packagedResources"))
    
    into("composeResources") {
        from(layout.buildDirectory.dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"))
    }
    into("composeResources/azkarapp.shared.generated.resources") {
        from(layout.buildDirectory.dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"))
    }
}

tasks.matching { it.name.startsWith("compileKotlinAndroid") || it.name.startsWith("process") }.configureEach {
    dependsOn(copyComposeResourcesWithPackage)
}