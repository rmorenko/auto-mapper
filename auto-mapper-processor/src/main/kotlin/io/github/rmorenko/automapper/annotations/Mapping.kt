package io.github.rmorenko.automapper.annotations


/**
 * Annotation for mapping properties.
 *
 * @property target The target property name.
 * @property transform The function call with target property or code execution result.
 * @property code Additional code or logic that execute
 */
annotation class Mapping(
    val target: String = "",
    val transform: String = "",
    val code: String = ""
)



