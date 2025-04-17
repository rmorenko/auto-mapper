package io.github.rmorenko.automapper.annotations

/**
 * Annotation to automatically generate class with same properties as source class
 *
 * @property pkg The package name of generated target class
 * @property name The name of generated target class
 * @property props List of additional properties declarations that will include in generated target class
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateTarget(
    val pkg: String = "",
    val name: String,
    val props: Array<AddTargetProperty> = []
)
