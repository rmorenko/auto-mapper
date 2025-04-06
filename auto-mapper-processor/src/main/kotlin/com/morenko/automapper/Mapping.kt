package com.morenko.automapper

/**
 * Annotation for mapping properties.
 *
 * @property code The code associated with the mapping. Default is an empty string.
 * @property type The type of the mapping, defined by the `MappingType` enum. Default is `MappingType.CODE`.
 * @property target The target property for the mapping. Default is an empty string.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mapping(
    val code: String = "",
    val type: MappingType = MappingType.CODE,
    val target: String = ""
)



