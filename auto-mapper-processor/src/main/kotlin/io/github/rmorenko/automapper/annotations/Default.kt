package io.github.rmorenko.automapper.annotations

/**
 * Annotation used with `@Mapping` to specify default values for properties during
 * the mapping process.
 *
 * @property target The target property for which the default value is specified.
 * @property code The code snippet that provides the default value for the target property.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Default(val target: String = "", val code: String = "")
