package io.github.rmorenko.automapper.exceptions

import kotlin.reflect.KClass

/**
 * Exception thrown when a required annotation argument is not present.
 *
 * @param annotation The type of the annotation.
 * @param argumentName The name of the missing argument.
 */
class AnnotationArgumentNotPresent(annotation: KClass<*>, argumentName: String) :
    RuntimeException("Annotation argument $argumentName not present in ${annotation.simpleName}")
