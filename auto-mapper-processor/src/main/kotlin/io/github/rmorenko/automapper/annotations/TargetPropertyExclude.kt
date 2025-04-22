package io.github.rmorenko.automapper.annotations

/**
 * Annotation use with `@GenerateTarget` that mark property in source class
 * that shouldn't add to target class
 *
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class TargetPropertyExclude
