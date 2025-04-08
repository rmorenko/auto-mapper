package com.morenko.automapper

/**
 * Exception thrown when a required annotation argument is not present.
 *
 * @param annotationName The name of the annotation.
 * @param argumentName The name of the missing argument.
 */
class AnnotationArgumentNotPresent(annotationName: String, argumentName: String) :
    RuntimeException("Annotation argument $annotationName not present in $argumentName")
