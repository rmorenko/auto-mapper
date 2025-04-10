package com.morenko.automapper.generators

import com.google.devtools.ksp.processing.KSPLogger
import com.morenko.automapper.model.MappingInfo


/**
 * Class responsible for generation expression based on mapping information in Mapping annotation.
 *
 * @property logger Logger for logging messages during processing.
 */
class PropertyExpressionGenerator(private val logger: KSPLogger) {

    /**
     * Generate expression based on the property name, property type, target entity name, and mapping information.
     *
     * @param propName The name of the property.
     * @param propertyClassName The name of property class
     * @param targetEntityName The name of the target class.
     * @param mappingInfo The mapping information.
     * @return The code expression for the function.
     */
    fun generate(propName: String, propertyClassName: String, targetEntityName: String?, mappingInfo: MappingInfo?) =
        generate(getArgumentPart(propName, propertyClassName, targetEntityName, mappingInfo), mappingInfo).also {
            logger.info("Generated expression $it")
        }

    private fun generate(argument: String, mappingInfo: MappingInfo?): String =
        mappingInfo?.let { m ->
            FunctionKind.functions(m)
                .also { logger.info("Functions types: $it") }
                .fold(argument) { result, fn ->
                    return@fold when (fn) {
                        FunctionKind.MAP -> "${m.mapFn}($result)"
                        FunctionKind.INVOKE -> "$result.${m.invokeFn}()"
                    }
                }
        } ?: argument

    private fun getArgumentPart(
        propName: String,
        propertyClassName: String,
        targetEntityName: String?,
        mappingInfo: MappingInfo?
    ): String = if (targetEntityName != null) {
        "this.$propName.map$propertyClassName" +
                "To$targetEntityName()"
    } else if (mappingInfo != null && mappingInfo.code.isNotEmpty()) {
        mappingInfo.code
    } else {
        "this.$propName"
    }.also {
        logger.info("Argument expression: $it")
    }


    private enum class FunctionKind {
        MAP, INVOKE;

        companion object {
            fun functions(mappingInfo: MappingInfo): List<FunctionKind> = when (mappingInfo.mapFirst) {
                true -> listOf(MAP to mappingInfo.mapFn, INVOKE to mappingInfo.invokeFn)
                false -> listOf(INVOKE to mappingInfo.invokeFn, MAP to mappingInfo.mapFn)
            }.filter {
                it.second.isNotEmpty()
            }.map {
                it.first
            }
        }
    }
}
