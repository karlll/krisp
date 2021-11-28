import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "com.ninjacontrol"
version = "0.0.1-SNAPSHOT"

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

task<JavaExec>("run-internal-tests") {
    dependsOn("testClasses")
    mainClass.set("com.ninjacontrol.krisp.TestRunnerKt")
    classpath = sourceSets["test"].runtimeClasspath
}
task<Exec>("run-mal-regression-tests") {
    dependsOn("jar")
    workingDir = File("./src/test/mal")
    commandLine = listOf("./regression-test.sh")
}
