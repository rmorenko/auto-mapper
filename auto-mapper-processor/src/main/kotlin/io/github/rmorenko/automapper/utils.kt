package io.github.rmorenko.automapper

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import io.github.rmorenko.automapper.exceptions.AnnotationArgumentNotPresent
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
fun KSClassDeclaration.getFirstAnnotation(annotationClass: KClass<*>): KSAnnotation? {
    return getAnnotations(annotationClass).firstOrNull()
}

/**
 * Extension function to retrieve annotations from a class declaration.
 *
 * @param annotationClass The class of the annotation to retrieve.
 * @return The list of `KSAnnotation` object representing the annotation, or null if not found.
 */
fun KSClassDeclaration.getAnnotations(annotationClass: KClass<*>): List<KSAnnotation> {
    return annotations.filter {
        annotationClass.simpleName == it.shortName.asString() &&
                it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                annotationClass.qualifiedName.toString()
    }.toList()
}

/**
 * Extension function to retrieve annotations from a property declaration.
 *
 * @param annotationClass The class of the annotation to retrieve.
 * @return The list of `KSAnnotation` object representing the annotation, or null if not found.
 */
fun KSPropertyDeclaration.getAnnotations(annotationClass: KClass<*>): List<KSAnnotation> {
    return annotations.filter {
        annotationClass.simpleName == it.shortName.asString() &&
                it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                annotationClass.qualifiedName.toString()
    }.toList()
}

/**
 * Extension function to retrieve the value of a specified argument from a collection of annotation arguments.
 *
 * @param argumentName The name of the argument to retrieve.
 * @return The value of the specified argument as type `T`, or null if not found.
 */
inline fun <reified T> Collection<KSValueArgument>.getAnnotationArgumentValue(argumentName: String): T? {
    return this.firstOrNull { it.name?.getShortName() == argumentName }?.value as? T
}

/**
 * Extension function to retrieve the value of a specified argument from a collection of annotation arguments.
 *
 * @param argumentName The name of the argument to retrieve.
 * @return The value of the specified argument as type `T`, or null if not found.
 * @throws AnnotationArgumentNotPresent if annotation argument is not present
 */
inline fun <reified T> Collection<KSValueArgument>.getRequiredAnnotationArgumentValue(argumentName: String): T {
    return this.firstOrNull { it.name?.getShortName() == argumentName }?.value as? T
        ?: throw AnnotationArgumentNotPresent(T::class, argumentName)
}



/**
 * Extension function to retrieve list of classes than annotated with some annotation
 *
 * @param annotation annotation type
 * @return List of class declarations
 */
fun Resolver.getClassDeclarations(annotation: KClass<*>): List<KSClassDeclaration> =
    getSymbolsWithAnnotation(annotation.qualifiedName.orEmpty())
.filterIsInstance<KSClassDeclaration>()
.toList()
