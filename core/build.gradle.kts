import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

task<JavaExec>("run-internal-tests") {
    dependsOn("testClasses")
    mainClass.set("com.ninjacontrol.krisp.TestRunnerKt")
    classpath = sourceSets["test"].runtimeClasspath
}
task<Exec>("run-mal-regression-tests") {
    dependsOn("copy-fat-jar")
    workingDir = File("./src/test/mal")
    commandLine = listOf("./regression-test.sh")
}
tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.ninjacontrol.krisp.MainKt"
    }
    archiveBaseName.set("krisp-core")
}
task<Jar>("fat-jar") {
    dependsOn("jar")
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile)).duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    archiveBaseName.set("krisp-core-all")
    with(tasks["jar"] as CopySpec)
}
task("copy-fat-jar") {
    dependsOn("fat-jar")
    doLast {
        copy {
            from("$buildDir/libs/krisp-core-all-${project.version}.jar")
            into("$buildDir/libs/")
            rename("(.+)-${project.version}(.+)", "$1$2")
        }
    }
}
