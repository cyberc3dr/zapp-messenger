dependencies {
    api(libs.kotlin.reflect)

    api(libs.kotlinx.serialization)
    api(libs.kotlinx.io)

    api(libs.korlibs.compression)
    api(libs.korlibs.crypto)
    
    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.dao)

    api(libs.postgres)
    api(libs.h2)
}