package com.morenko.automapper

/**
 * Data class representing mapping information for a property.
 *
 * @property code The code associated with the mapping, if any.
 * @property type The type of the mapping, defined by the `MappingType` enum. Default is `MappingType.CODE`.
 * @property target The target property for the mapping, if any.
 */
data class MappingInfo(
    val code: String?,
    val type: MappingType = MappingType.CODE,
    val target: String?,
)

/**
 * Enum class representing the type of mapping.
 */
enum class MappingType {
    /** Represents a code-based mapping. */
    CODE,

    /** Represents a function-based mapping. */
    FUNCTION,

    /** Represents an extension function-based mapping. */
    EXTENSION_FUNCTION
}
