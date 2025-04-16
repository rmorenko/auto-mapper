package io.github.rmorenko.automapper.model

/**
 * Data class for storing information from 'AutoMapper' annotation.
 *
 * @property targetPackage The package for target class
 * @property targetName The name of target class
 * @property defaults The information about defaults
 * @property additionalImports The additional imports that added to generated file
 * @property excludes The properties that should be excluded from mapping
 */
data class AutoMapperInfo(
    val targetPackage: String,
    val targetName: String,
    val defaults: Set<DefaultInfo> = emptySet(),
    val additionalImports: Set<String> = emptySet(),
    val excludes: Set<String> = emptySet(),
)
