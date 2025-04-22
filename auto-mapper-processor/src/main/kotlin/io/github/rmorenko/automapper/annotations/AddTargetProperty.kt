package io.github.rmorenko.automapper.annotations

/**
 * Annotation to declare additional property that will add to generated target class
 *
 * @property name The name of additional property
 * @property pkg The package name of additional property
 * @property className The name of class additional property
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AddTargetProperty(
    val name: String,
    val pkg: String = "",
    val className: String,
)
