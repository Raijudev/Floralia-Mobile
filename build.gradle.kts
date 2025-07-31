// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Si estás usando AGP 8.x o superior, el formato de cómo se aplica el plugin ha cambiado ligeramente.
    // Usamos 'id("...") version "..." apply false' para la definición de plugins de nivel superior.
    // Las versiones deben coincidir con las que tienes en libs.versions.toml en la sección [versions]
    id("com.android.application") version "8.10.1" apply false // Usar version.ref para AGP
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false // Usar version.ref para Kotlin
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false // Usar version.ref para Compose
    id("com.google.gms.google-services") version "4.4.2" apply false // Usar version.ref para Google Services

    // Si quieres seguir usando alias, el formato podría ser:
    // alias(libs.plugins.android.application) apply false
    // alias(libs.plugins.kotlin.android) apply false
    // alias(libs.plugins.kotlin.compose) apply false
    // alias(libs.plugins.google.gms.google.services) apply false
    // Pero el error sugiere que los alias no se están resolviendo correctamente aquí.
    // La forma 'id("...") version "..."' es más robusta para el build.gradle de nivel superior.
}