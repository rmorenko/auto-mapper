package com.morenko.automapper

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import kotlin.reflect.KClass

/**
 * Extension function to retrieve the value of a specified annotation property.
 *
 * @param name The name of the annotation property to retrieve.
 * @return The value of the specified annotation property as a trimmed string, or null if not found.
 */
fun KSAnnotation.getAnnotationProperty(name: String): String? {
    return this.arguments.getAnnotationProperty(name)
}

/**
 * Extension function to retrieve the value of a specified annotation property.
 *
 * @param name The name of the annotation property to retrieve.
 * @return The value of the specified annotation property as a trimmed string, or null if not found.
 */
fun Collection<KSValueArgument>.getAnnotationProperty(name: String): String? {
    return firstOrNull { agr ->
        agr.name?.getShortName() == name
    }?.value?.toString()?.trim()
}

/**
 * Extension function to retrieve a specific annotation from a class declaration.
 *
 * @param annotationClass The class of the annotation to retrieve.
 * @return The `KSAnnotation` object representing the annotation, or null if not found.
 */
fun KSClassDeclaration.getAnnotation(annotationClass: KClass<*>): KSAnnotation? {
    return this.annotations.firstOrNull {
        annotationClass.simpleName == it.shortName.asString()
    }
}

/**
 * Extension function to retrieve the value of a specified argument from a collection of annotation arguments.
 *
 * @param argumentName The name of the argument to retrieve.
 * @return The value of the specified argument as type `T`, or null if not found.
 */
fun <T> Collection<KSValueArgument>.getAnnotationArgumentValue(argumentName: String): T? {
    return this.firstOrNull {
        argumentName == it.name?.getShortName()
    }?.value as? T
}
