package io.github.rmorenko.automapper.annotations

import kotlin.reflect.KClass

/**
 * Annotation to automatically generate a mapper for the specified target class.
 *
 * @property target The target class to which the mapping should be generated.
 * @property targetName The name of target class to which the mapping should be generated.
 * @property targetPkg The package of target class to which the mapping should be generated.
 * @property defaults An array of default values to be used during the mapping process.
 * @property imports An array of import statements required for the generated mapper.
 * @property exclude A vararg of property names to be excluded from the mapping.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoMapper(
    val target: KClass<*> = Unit::class,
    val targetName: String = "",
    val targetPkg: String = "",
    val defaults: Array<Default> = [],
    val imports: Array<String> = [],
    vararg val exclude: String = []
)

