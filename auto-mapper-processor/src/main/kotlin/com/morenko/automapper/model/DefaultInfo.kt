package com.morenko.automapper.model

import com.google.devtools.ksp.symbol.KSAnnotation
import com.morenko.automapper.getAnnotationProperty

/**
 * Data class representing default information for a mapping.
 *
 * @property target The target property for the default value.
 * @property code The code associated with the default value.
 */
data class DefaultInfo(val target: String, val code: String)

/**
 * Extension function to convert a `KSAnnotation` to a `DefaultInfo` object.
 *
 * @return A `DefaultInfo` object containing the target and code from the annotation.
 */
fun KSAnnotation.toDefaultInfo() = DefaultInfo(
    this.getAnnotationProperty("target").orEmpty(),
    this.getAnnotationProperty("code").orEmpty()
)
