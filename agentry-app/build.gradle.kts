plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":agentry-core"))
    implementation(project(":agentry-persistence"))
    implementation(project(":agentry-api"))
    implementation(project(":agentry-ci-gateway"))
    implementation(project(":agentry-cli"))
    implementation(project(":agentry-dashboard"))
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.actuator)

    testImplementation(libs.spring.boot.starter.test)
}

springBoot {
    mainClass = "com.agentry.app.AgentryApplication"
}
