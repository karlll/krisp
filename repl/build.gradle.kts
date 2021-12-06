import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.aesh:readline:2.2")
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
task<Jar>("fat-jar") {
    dependsOn("jar")
    manifest {
        attributes["Main-Class"] = "com.ninjacontrol.krisp.ReplKt"
        attributes["Implementation-Version"] = project.version
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile)).duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    archiveBaseName.set("krisp-repl")
    with(tasks["jar"] as CopySpec)
}
