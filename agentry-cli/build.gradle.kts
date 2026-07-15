plugins {
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(project(":agentry-core"))
    implementation(libs.spring.shell.starter)

    testImplementation(libs.spring.boot.starter.test)
}
