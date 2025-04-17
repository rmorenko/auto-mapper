package io.github.rmorenko.automapper.annotations

/**
 * Annotation to set property type in generated target class
 *
 * @property pkg The package name of property
 * @property className The name of class property
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateTargetPropertyType(
    val pkg: String = "",
    val className: String,
)
