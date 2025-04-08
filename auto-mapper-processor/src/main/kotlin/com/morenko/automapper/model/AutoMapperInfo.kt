package com.morenko.automapper.model

import com.google.devtools.ksp.symbol.KSType

/**
 * Data class for storing information from 'AutoMapper' annotation.
 *
 * @property sourcePackage The package for source class
 * @property sourceName The name of source class
 * @property targetType The type of target class
 * @property defaults The information about defaults
 * @property excludes The properties that should be excluded from mapping
 */
data class AutoMapperInfo(
    val sourcePackage: String,
    val sourceName: String,
    val targetType: KSType,
    val defaults: Set<DefaultInfo> = emptySet(),
    val additionalImports: Set<String> = emptySet(),
    val excludes: Set<String> = emptySet(),
)
