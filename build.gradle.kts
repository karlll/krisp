import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "com.ninjacontrol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.ninjacontrol.krisp.MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile)).duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
