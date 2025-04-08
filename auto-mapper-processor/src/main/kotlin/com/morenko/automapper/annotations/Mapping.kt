package com.morenko.automapper.annotations

/**
 * Annotation for mapping properties.
 *
 * @property target The target property name.
 * @property mapFn The function call with target property or code execution result.
 * @property invokeFn The function to invoke on the source property or on code execution result.
 * @property code Additional code or logic that execute
 * @property mapFirst Indicates whether the map function should be executed first.
 */
annotation class Mapping(
    val target: String = "",
    val mapFn: String = "",
    val invokeFn: String = "",
    val code: String = "",
    val mapFirst: Boolean = true
)



