package com.morenko.automapper.model

/**
 * Data class for storing mapping information.
 *
 * @property target The target property name.
 * @property mapFn The function to map the target property.
 * @property invokeFn The function to invoke on the target object.
 * @property code Additional code or logic to apply.
 * @property mapFirst Indicates whether the map function should be executed first.
 */
data class MappingInfo(
    val target: String = "",
    val mapFn: String = "",
    val invokeFn: String = "",
    val code: String = "",
    val mapFirst: Boolean = true
)
