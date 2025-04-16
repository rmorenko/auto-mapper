package io.github.rmorenko.automapper.model

/**
 * Data class for storing mapping information.
 *
 * @property target The target property name.
 * @property functionName The simple name of function to map the target property.
 * @property isExtension Flag of extension function.
 * @property code Additional code or logic to apply.
 */
data class MappingInfo(
    val target: String = "",
    val functionName: String = "",
    val code: String = "",
    val isExtension: Boolean = false
)
