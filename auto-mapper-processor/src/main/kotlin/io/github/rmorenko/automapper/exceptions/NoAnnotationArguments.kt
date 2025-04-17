package io.github.rmorenko.automapper.exceptions

import kotlin.reflect.KClass

/**
 * Exception thrown when no annotation arguments presents
 *
 * @param annotation The type of the annotation.
 */
class NoAnnotationArguments(annotation: KClass<*>) :
    RuntimeException("No arguments present in annotation: ${annotation.simpleName}")
