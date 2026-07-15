plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "agentry"

include(
    "agentry-core",
    "agentry-persistence",
    "agentry-api",
    "agentry-ci-gateway",
    "agentry-cli",
    "agentry-dashboard",
    "agentry-app"
)
