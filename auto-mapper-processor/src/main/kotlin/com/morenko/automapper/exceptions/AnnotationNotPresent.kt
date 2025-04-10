package com.morenko.automapper.exceptions

import kotlin.reflect.KClass

/**
 * Exception thrown when a required annotation is not present.
 *
 * @param annotation The type of the annotation.
 */
class AnnotationNotPresent(annotation: KClass<*>) :
    RuntimeException("Annotation ${annotation.simpleName} not present")

