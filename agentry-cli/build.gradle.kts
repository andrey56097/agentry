plugins {
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(project(":agentry-core"))
    implementation(project(":agentry-persistence"))
    implementation(libs.spring.shell.starter)
    implementation(libs.okhttp)
    implementation(libs.jackson.databind)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)

    testImplementation(libs.spring.boot.starter.test)
}
