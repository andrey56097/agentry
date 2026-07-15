plugins {
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(project(":agentry-core"))
    implementation(project(":agentry-persistence"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)

    testImplementation(libs.spring.boot.starter.test)
}
