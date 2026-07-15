plugins {
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(project(":agentry-api"))
    implementation(libs.spring.boot.starter.web)

    testImplementation(libs.spring.boot.starter.test)
}

val buildReact by tasks.registering(Exec::class) {
    workingDir = file("frontend")
    commandLine("npm", "run", "build")
    outputs.dir("src/main/resources/static")
}

tasks.named("compileJava") {
    dependsOn(buildReact)
}
