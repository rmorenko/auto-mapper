package io.github.rmorenko.automapper.generators

import com.google.devtools.ksp.processing.KSPLogger
import io.github.rmorenko.automapper.model.MappingInfo


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

    private fun generate(argument: String, mappingInfo: MappingInfo?): String {
        logger.info("generate call with $argument and ${mappingInfo?.functionName}")
        return if (mappingInfo == null || mappingInfo.functionName.isEmpty()) {
            argument
        } else if (mappingInfo.isExtension) {
            "$argument.${mappingInfo.functionName}()"
        } else {
            "${mappingInfo.functionName}($argument)"
        }
    }


    private fun getArgumentPart(
        propName: String,
        propertyClassName: String,
        targetEntityName: String?,
        mappingInfo: MappingInfo?
    ): String = if (mappingInfo != null && mappingInfo.code.isNotEmpty()) {
        mappingInfo.code
    } else if (targetEntityName != null && targetEntityName.isNotBlank()) {
        "this.$propName.map$propertyClassName" +
                "To$targetEntityName()"
    } else {
        "this.$propName"
    }.also {
        logger.info("Argument expression: $it")
    }
}
