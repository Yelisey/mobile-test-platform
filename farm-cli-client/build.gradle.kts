plugins {
    application
    distribution
    kotlin("jvm")
}

val appVersion: String by project
version = appVersion
group = "com.atiurin.atp.farmcliclient"

application {
    mainClass.set("com.atiurin.atp.farmcliclient.FarmCliClientKt")
    applicationName = "farm-cli-client"
    setBuildDir("build/app")
}

distributions {
    getByName("main") {
        distributionBaseName.set("farm-cli-client")
    }
}
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":farm-core"))
    implementation(project(":farm-kmp-client"))
    implementation(project(":cli-support"))
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging.jvm)
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.7.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
