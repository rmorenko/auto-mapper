package com.morenko.automapper.generators

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.morenko.automapper.annotations.AutoMapper
import com.morenko.automapper.getAnnotation
import com.morenko.automapper.getAnnotationArgumentValue
import com.morenko.automapper.model.AutoMapperInfo
import com.morenko.automapper.model.DefaultInfo
import com.morenko.automapper.model.MappingInfo
import com.squareup.kotlinpoet.ksp.toClassName

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
     * @param mappings The information from 'Mapping' annotation for each property in source class.
     * @param autoMapperInfo The resolved information from AutoMapper annotation attributes
     * @return generated mappings
     */
    fun generate(
        classDeclaration: KSClassDeclaration,
        mappings: Map<String, MappingInfo>,
        autoMapperInfo: AutoMapperInfo,
    ): String {
        val targetProperties = getTargetProperties(classDeclaration)
        logger.info("Target class properties:  $targetProperties")
        return classDeclaration.getAllProperties()
            .filter { prop ->
                prop.simpleName.asString() !in autoMapperInfo.excludes
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
                if (sourceProperty !in targetProperties) {
                    return@map ""
                }
                return@map "    " +
                        "$sourceProperty = " +
                        propertyExpressionGenerator.generate(propName, propType.toClassName().simpleName,
                            targetName, mappingInfo)
            }.filter { str ->
                str.isNotEmpty()
            }.joinToString(",\n") + addDefaults(autoMapperInfo.defaults)
    }


    private fun getTargetProperties(classDeclaration: KSClassDeclaration): Set<String> {
        val type = classDeclaration.annotations
            .firstOrNull {
                it.shortName.asString() == AutoMapper::class.simpleName.toString()
            }?.arguments?.first {
                it.name?.asString() == "target"
            }?.value as? KSType ?: return emptySet()

        return (type.declaration as KSClassDeclaration).getAllProperties()
            .map { it.simpleName.asString() }
            .toSet()
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

}
