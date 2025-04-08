package com.morenko.automapper.generators

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.morenko.automapper.annotations.AutoMapper
import com.morenko.automapper.getAnnotation
import com.morenko.automapper.getAnnotationArgumentValue
import com.morenko.automapper.model.MappingInfo

/**
 * Class responsible for generation properties mappings based on class declaration
 * and information from AutMapper annotation.
 *
 * @property logger Logger for logging messages during processing.
 */
class PropertyMappingsGenerator(private val logger: KSPLogger) {

    private val propertyExpressionGenerator = PropertyExpressionGenerator(logger)

    /**
     * @param classDeclaration The class declaration to generate the mapping function for.
     * @param mappings information from 'Mapping' annotation for each property in source class.
     * @param excludes The set of properties names, that excludes from mappings\
     * @return generated mappings
     */
    fun generate(
        classDeclaration: KSClassDeclaration,
        mappings: Map<String, MappingInfo>,
        excludes: Set<String>
    ): String {
        val sourceProperties = getSourceProperties(classDeclaration)
        logger.info("Target properties:  $sourceProperties")
        return classDeclaration.getAllProperties()
            .filter {
                it.simpleName.asString() !in excludes
            }
            .map { prop ->
                logger.info("Create mapping for :  $prop")
                val propName = prop.simpleName.asString()
                val propType = prop.type.resolve()

                val mappingInfo = mappings[propName]
                val targetName = (propType.declaration as? KSClassDeclaration)?.let { classDecl ->
                    val mappedType = classDecl.getAnnotation(AutoMapper::class)?.arguments
                        ?.getAnnotationArgumentValue<KSType>(
                            "target"
                        )
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
                return@map "    " +
                        "$sourceProperty = " +
                        propertyExpressionGenerator.generate(propName, propType, targetName, mappingInfo)
            }.filter { str ->
                str.isNotEmpty()
            }.joinToString(",\n")
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
