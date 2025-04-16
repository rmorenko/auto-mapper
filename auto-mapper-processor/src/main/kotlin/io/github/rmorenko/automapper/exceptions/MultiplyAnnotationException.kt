package io.github.rmorenko.automapper.exceptions

import kotlin.reflect.KClass

/**
 * Exception thrown when a declared more than one annotation
 *
 * @param annotation The type of the annotation.
 */
class MultiplyAnnotationException(annotation: KClass<*>) :
    RuntimeException("Multiply annotations ${annotation.simpleName} is not allowed")

