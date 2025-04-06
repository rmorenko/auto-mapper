package com.morenko.automapper

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Class responsible for generating mapping code based on provided mappings and defaults.
 *
 * @property logger Logger for logging information during code generation.
 */
class MappingCodeGenerator(private val logger: KSPLogger) {

    /**
     * Resolves mappings for the given class declaration.
     *
     * @param classDeclaration The class declaration to resolve mappings for.
     * @param mappings A map of property names to their corresponding mapping information.
     * @param defaults A set of default mapping information.
     * @param excludes A set of property names to exclude from mapping.
     * @return A string representing the resolved mappings.
     */
    fun resolveMappings(
        classDeclaration: KSClassDeclaration,
        mappings: Map<String, MappingInfo>,
        defaults: Set<DefaultInfo>,
        excludes: Set<String>
    ): String {
        val sourceProperties = getSourceProperties(classDeclaration)
        logger.info("Target properties:  $sourceProperties")
        return classDeclaration.getAllProperties()
            .filter {
                it.simpleName.asString() !in excludes
            }
            .map { prop ->
                logger.info("Resolve mapping for :  $prop")
                val propName = prop.simpleName.asString()
                val propType = prop.type.resolve()

                val mappingInfo = mappings[propName]
                val code = mappingInfo?.code.orEmpty()
                val mappingType = mappingInfo?.type ?: MappingType.CODE

                logger.info("Resolve mapping for $code with type $mappingType")
                val targetName = (propType.declaration as? KSClassDeclaration)?.let { classDecl ->
                    val mappedType = classDecl.getAnnotation(AutoMapper::class)?.arguments
                        ?.getAnnotationArgumentValue<KSType>(
                        "target")
                    mappedType?.declaration?.simpleName?.asString()
                }
                val sourceProperty = if (mappingInfo?.target == null || mappingInfo.target.isEmpty()) {
                    propName
                } else {
                    mappingInfo.target
                }
                logger.info("Source property: $sourceProperty")
                if (sourceProperty !in sourceProperties) {
                    return@map ""
                }
                return@map "    $sourceProperty = ${
                    wrapWithFunction(getArgumentCodeSnippet(propName, propType, targetName, mappingInfo), mappingInfo)
                }"
            }.filter { str ->
                str.isNotEmpty()
            }.joinToString(",\n") + addDefaults(defaults)
    }

    private fun addDefaults(defaults: Set<DefaultInfo>): String {
        val defaultsStr = defaults.joinToString(",\n") {
            "${it.target} = ${it.code}"
        }
        if (defaultsStr.isNotEmpty()) {
            logger.info("Default: $defaultsStr")
            return ",\n    $defaultsStr"
        }
        logger.info("Default: $defaultsStr")
        return defaultsStr
    }

    private fun getArgumentCodeSnippet(
        propName: String,
        propType: KSType,
        targetEntityName: String?,
        mappingInfo: MappingInfo?
    ): String = if (targetEntityName != null) {
        "this.$propName.map${propType.toClassName().simpleName}" +
                "To$targetEntityName()"
    } else if (mappingInfo != null && mappingInfo.type == MappingType.CODE && mappingInfo.code?.isNotEmpty() == true) {
        mappingInfo.code
    } else {
        "this.$propName"
    }

    private fun wrapWithFunction(argument: String, mappingInfo: MappingInfo?): String {
        if (mappingInfo?.code == null || mappingInfo.code.isEmpty()
        ) {
            return argument
        }
        return when (mappingInfo.type) {
            MappingType.FUNCTION -> "${mappingInfo.code}($argument)"
            MappingType.EXTENSION_FUNCTION -> "$argument.${mappingInfo.code}()"
            MappingType.CODE -> argument
        }
    }

    private fun getSourceProperties(classDeclaration: KSClassDeclaration): Set<String> {
        val type = classDeclaration.annotations
            .firstOrNull { it.shortName.asString() == "AutoMapper" }
            ?.arguments?.firstOrNull()?.value as? KSType
            ?: return emptySet()

        return (type.declaration as KSClassDeclaration).getAllProperties()
            .map { it.simpleName.asString() }
            .toSet()
    }
}
